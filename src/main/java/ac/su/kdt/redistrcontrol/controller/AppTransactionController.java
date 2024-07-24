package ac.su.kdt.redistrcontrol.controller;

import ac.su.kdt.redistrcontrol.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class AppTransactionController {
    private final RedisService redisService;

    // 키 발급
    @GetMapping("/transaction-key")
    public String getTransactionKey(

    ) {
        // Transaction Key 생성 로직은 계획적으로 수립 필요
        // 1) Order 객체 관련 Transaction 의 경우
        // -> 수량 한정판매에서 HTTP 요청 Validation
        //    (수량 초과 방지 : 목적 자원 수량 관리 -> 키)
        //    => 해당 자원(한정판매 상품)의 redis 등록 키 (수량)

        // 2) Product 객체에서 등록 Admin 업체 고객 업무 시
        // -> 동일 품목 중복 등록을 방지
        //    (따닥 방지 : 요청 고유성 -> 키)
        //    => 매 회차 클라이언트가 Transaction 권한 보유 전
        //       키를 발급받도록 호출 절차를 정의한 후 제공
        return UUID.randomUUID().toString();
    }

    // 키 검증
    @GetMapping("/transaction-result-test")
    public ResponseEntity<String> transactionResultTest(
        @RequestParam String key
    ) {
        boolean isTransactionSuccess = redisService.setIfAbsent(
            key, LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(
            isTransactionSuccess? "Transaction Success": "Transaction Fail",
            isTransactionSuccess? HttpStatus.OK: HttpStatus.CONFLICT
        );
    }
}
