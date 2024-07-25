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

    // Post 요청 전 TransactionKey 발급된 것을 전제로 함
    @PostMapping("/with-cache1")
    public ResponseEntity<Product> createProduct2(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ) {
        // 요청 수신 후, transactionKey 부터 검사
        Optional<Product> createdProduct = redisService.setIfAbsentGetIfPresent(
            // 복잡도가 높아서 다른 코드 흐름으로 대체
            transactionKey,
            // LocalDateTime.now().toString()
            product.toEntity()
        );
        if (createdProduct.isEmpty()) {
            try {
                Product createdProductNow = productService.createProduct(product.toEntity());
                return new ResponseEntity<>(createdProductNow, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(createdProduct.get(), HttpStatus.CREATED);
    }

    @PostMapping("/cache-write-test")
    public ResponseEntity<Boolean> writeProductCache(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ) {
        // product 가 수신되면 set 부터 수행 동작 확인
        boolean isSet = redisService.setProduct(transactionKey, product.toEntity());
        return new ResponseEntity<>(isSet, HttpStatus.OK);
    }

    @GetMapping("/cache-read-test")
    public ResponseEntity<Product> readProductCache(
        @RequestParam(name = "transaction-key") String transactionKey
    ) {
        Product productFromCache = redisService.getProduct(transactionKey);
        return new ResponseEntity<>(productFromCache, HttpStatus.OK);
    }

    // 1) transactionKey 에 대한 캐시값이 있는지 확인한다 (get 호출)
    //    1-1) 없는 경우 정상 흐름 진행
    //       1-2-1) 정상 흐름 진행 완료 후 응답데이터 캐싱
    //    1-2) 있는 경우 캐싱된 데이터 받아서 응답
    @PostMapping("/cached-create")
    public ResponseEntity<Product> cachedProductCreate(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ){
        // TODO: 위 로직들을 참고해서 캐시 활용 응답 플로우를 구현해 보세요
        return null; // 리턴값 플레이스홀딩
    }
}
