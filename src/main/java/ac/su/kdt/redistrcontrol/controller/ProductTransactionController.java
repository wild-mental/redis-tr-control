package ac.su.kdt.redistrcontrol.controller;

import ac.su.kdt.redistrcontrol.domain.Product;
import ac.su.kdt.redistrcontrol.domain.ProductForm;
import ac.su.kdt.redistrcontrol.service.ProductService;
import ac.su.kdt.redistrcontrol.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

// [과제] Redis Transaction 실제 로직 적용
// 1) ProductDTO 형태로 만들고 예제 호출 POSTMAN 생성
// 2) ProductTransactionController 를 만들고,
//    redis 에서 발급받은 키가 제출되어야만 후속 create 로직 수행하도록 엔드포인트 구현
// 3) 10초 범위를 따닥 및 중복 호출 방지하고자 하는 시간 구간에 맞추어서 조절 및 테스트 수행
//   => 프론트엔드 이벤트와 붙여서 테스트하면 가장 좋음

@RestController
@RequiredArgsConstructor
@RequestMapping("/products-transaction")
public class ProductTransactionController {
    private final ProductService productService;
    private final RedisService redisService;

    // ####################################################
    // 1) Transaction Key 중복 시 회복 없이 에러 응답하는 엔드포인트
    // ####################################################
    // Post 요청 전 TransactionKey 발급된 것을 전제로 함
    @PostMapping
    public ResponseEntity<Product> createProduct(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ) {
        // 요청 수신 후, transactionKey 부터 검사
        boolean isTransactionSuccess = redisService.setIfAbsent(
            transactionKey, LocalDateTime.now().toString()
        );
        if (isTransactionSuccess) {
            try {
                Product createdProduct = productService.createProduct(product.toEntity());
                return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // 이미 요청된 상품 생성 Transaction Key 가 재수신된 경우 에러 응답!
        // -> 에러응답으로는 충분하지 않다!
        // => 고객의 에러 페이지 수신 시 뒤로가기 및 재호출을 수많은 횟수 반복하면서 결국 TTL 초과하며 중복 요청 발생 가능
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    // ####################################################
    // 2) Transaction Key 중복 시 흐름 회복을 위한 Read-Write 분리
    // ####################################################
    // 2-1) 캐시 write
    @PostMapping("/cache-write-test")
    public ResponseEntity<Boolean> writeProductCache(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ) {
        // product 가 수신되면 set 부터 수행 동작 확인
        boolean isSet = redisService.setProduct(transactionKey, product.toEntity());
        return new ResponseEntity<>(isSet, HttpStatus.OK);
    }
    // 2-2) 캐시 read
    @GetMapping("/cache-read-test")
    public ResponseEntity<Product> readProductCache(
        @RequestParam(name = "transaction-key") String transactionKey
    ) {
        Optional<Product> productFromCache = redisService.getProduct(transactionKey);
        return productFromCache.map(
            product -> new ResponseEntity<>(product, HttpStatus.OK)  // Cache Hit
        ).orElseGet(
            () -> new ResponseEntity<>(HttpStatus.NOT_FOUND)         // Cache Fail
        );
    }

    // ####################################################
    // 3) Transaction Key 중복 시 흐름 회복을 적용한 상품 등록
    // ####################################################
    // 3-1) setIfAbsent 로직 핸들링 단계 구현
    @PostMapping("/cached-create")
    public ResponseEntity<Product> cachedProductCreate(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ){
        // 1. 더미값으로 캐시 키 등록 시도
        boolean isLockObtained = redisService.setIfAbsent(transactionKey, "locked");
        if (!isLockObtained) {
            // 1-1. 더미값 캐시 키 등록 실패 시 해당 키로 캐싱된 데이터 read
            Optional<Product> cachedProduct = redisService.getProduct(transactionKey);
            if (cachedProduct.isEmpty()) {
                // 1-1-1. 캐싱된 데이터가 더미값인 경우 2회 재시도하며 상품 데이터로 업데이트 되는지 검사
                for (int i = 0; i < 2; i++) {
                    cachedProduct = redisService.getProduct(transactionKey);
                    // 1-1-1-1. 재시도 과정에서 상품 데이터로 조회될 경우 응답 반환
                    if (cachedProduct.isPresent()) {
                        // [회복] 앞선 DB Write 작업의 결과값을 읽어와서 회복 응답을 수행
                        return new ResponseEntity<>(cachedProduct.get(), HttpStatus.OK);
                    }
                }
                //     1-1-1-2. 재시도 과정에서 상품 데이터로 조회되지 않을 경우 실패 응답 반환
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // 캐시 에러로 메시지 상세화
            }
            // 1-1-2. 캐싱된 데이터가 상품 데이터로 반환될 경우 응답 반환
            // [회복] 앞선 DB Write 작업의 결과값을 읽어와서 회복 응답을 수행
            return new ResponseEntity<>(cachedProduct.get(), HttpStatus.OK);
        }
        // 1-2. 더미값 캐시 키 등록 성공 시 상품 등록
        Product createdProduct;
        try {
            // 실제 DB 접근 작업은 아래 라인에서만 수행됨
            createdProduct = productService.createProduct(product.toEntity());
        } catch (RuntimeException e) {
            // 1-2-1. 상품 등록 실패 시 키 삭제 후 에러 응답 반환
            redisService.deleteKey(transactionKey);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // DB 에러로 메시지 상세화
        }
        //     1-2-2. 상품 등록 성공 시 등록된 상품 id 를 포함한 상품 데이터 캐싱 시도
        boolean isCached = redisService.setProduct(transactionKey, createdProduct);
        //       1-2-1-1. 상품 데이터 캐싱 성공 시 응답 반환
        if (isCached) {
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        }
        //       1-2-1-2. 상품 데이터 캐싱 실패 시 transaction 키 삭제 및 상품 Transaction 롤백 후 에러 응답 반환
        redisService.deleteKey(transactionKey);
        productService.deleteProduct(createdProduct.getId());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // 캐시 에러로 메시지 상세화
    }

    // 3-2) service 클래스로 주요 작업을 넘겨 controller 코드 단순화
    //      setIfAbsentGetIfPresent 메서드 로직 다듬어야 함
//    @PostMapping("/with-cache1")
//    public ResponseEntity<Product> createProduct2(
//        @RequestParam(name = "transaction-key") String transactionKey,
//        @RequestBody ProductForm product
//    ) {
//        // 요청 수신 후, transactionKey 부터 검사
//        Optional<Product> createdProduct = redisService.setIfAbsentGetIfPresent(
//            transactionKey,
//            product.toEntity()  // LocalDateTime.now().toString()
//        );
//        if (createdProduct.isEmpty()) {
//            try {
//                Product createdProductNow = productService.createProduct(product.toEntity());
//                return new ResponseEntity<>(createdProductNow, HttpStatus.CREATED);
//            } catch (Exception e) {
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//        return new ResponseEntity<>(createdProduct.get(), HttpStatus.CREATED);
//    }
}
