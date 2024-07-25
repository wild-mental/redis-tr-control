package ac.su.kdt.redistrcontrol.service;

import ac.su.kdt.redistrcontrol.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplateDb0;
    private final StringRedisTemplate redisTemplateDb1;
    private final StringRedisTemplate redisTemplateDb2;

    // Transaction 검사용 메서드로 사용
    public boolean setIfAbsent(String key, String value) {
        return Boolean.TRUE.equals(  // null 값까지 커버하도록 핸들링
            redisTemplateDb0.opsForValue().setIfAbsent(
                key,
                value,  // 사용 시나리오에 따라서 키 값을 아무 값이나 쓰지 않고 실제 Caching 데이터로 쓰면 좋다
                Duration.ofSeconds(10)  // 사용 시나리오에 따라 적절히 조정
            )
        );
    }

    // TODO : ProductTransactionService 로 이동
    // DB 캐싱을 겸하는 Transaction 검사용 메서드
    public Optional<Product> setIfAbsentGetIfPresent(String key, Product value) {
        // 1) Object -> JSON String 언파싱 수행
        String jsonValue;
        //
        boolean isSet = Boolean.TRUE.equals(
            redisTemplateDb0.opsForValue().setIfAbsent(
                key,
                "",
                // jsonValue,  // Product 객체를 JSON String 으로 저장
                Duration.ofSeconds(300)  // 10분간 재사용 가능한 캐시 유지
            )
        );
        if (isSet) {
            // 캐시에서 받아온 값을 응답 (String 을 Product 로 파싱)
            // 2) JSON String -> Object 파싱
            String cachedProductString = redisTemplateDb0.opsForValue().get(key);
            Product cachedProduct;
            return Optional.of(
                new Product()
                // cachedProduct
            );  // Product 객체 형태로 반환
        }
        // 현재 요청이 최초 요청이므로 바로 생성
        return Optional.empty();
    }
}
