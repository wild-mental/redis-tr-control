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
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    // Post 요청 전 TransactionKey 발급된 것을 전제로 함
    @PostMapping("/with-cache")
    public ResponseEntity<Product> createProduct2(
        @RequestParam(name = "transaction-key") String transactionKey,
        @RequestBody ProductForm product
    ) {
        // 요청 수신 후, transactionKey 부터 검사
        Optional<Product> createdProduct = redisService.setIfAbsentGetIfPresent(
            transactionKey, LocalDateTime.now().toString()
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
}
