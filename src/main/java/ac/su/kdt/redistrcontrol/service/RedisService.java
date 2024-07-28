package ac.su.kdt.redistrcontrol.service;

import ac.su.kdt.redistrcontrol.domain.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplateDb0;
    private final RedisTemplate<String, Object> redisObjTemplateDb0;
    private final ObjectMapper objectMapper;

    // ####################################################
    // 1) Transaction 검사용 기본 메서드
    // ####################################################
    public boolean setIfAbsent(String key, String value) {
        return Boolean.TRUE.equals(  // null 값까지 커버하도록 핸들링
            redisTemplateDb0.opsForValue().setIfAbsent(
                key,
                value,  // 사용 시나리오에 따라서 키 값을 아무 값이나 쓰지 않고 실제 Caching 데이터로 쓰면 좋다
                Duration.ofSeconds(10)  // 사용 시나리오에 따라 적절히 조정
            )
        );
    }

    // ####################################################
    // 2) Product 데이터 캐싱을 위한 setter, getter 메서드
    // ####################################################
    public boolean setProduct(String transactionKey, Product product) {
        try {
            String productJsonString = objectMapper.writeValueAsString(product);
            return Boolean.TRUE.equals(
                redisTemplateDb0.opsForValue().setIfAbsent(
                    transactionKey, productJsonString, Duration.ofMinutes(10)
                )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Product> getProduct(String transactionKey) {
        String productJsonString = redisTemplateDb0.opsForValue().get(transactionKey);
        try {
            return Optional.of(
                objectMapper.readValue(productJsonString, Product.class)
            );
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    // ####################################################
    // 3) Entity 데이터 캐싱을 위한 범용화된 setter, getter 메서드
    // ####################################################
    public boolean setObject(String transactionKey, Object object) {
        return Boolean.TRUE.equals(
            redisObjTemplateDb0.opsForValue().setIfAbsent(
                transactionKey, object, Duration.ofMinutes(10)
            )
        );
    }

    public Optional<Object> getObject(String transactionKey, Object object) {
        Object cachedObj = redisObjTemplateDb0.opsForValue().get(transactionKey);
        try {
            return Optional.of(
                objectMapper.readValue(
                    Objects.requireNonNull(cachedObj).toString(),
                    object.getClass()
                )
            );
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public boolean deleteKey(String transactionKey) {
        return Boolean.TRUE.equals(redisTemplateDb0.delete(transactionKey));
    }

    // ####################################################
    // 4) DB 캐싱을 겸하는 Transaction 검사용 메서드
    // ####################################################
//    public Optional<Product> setIfAbsentGetIfPresent(String key, Product value) {
//        // 1) Object -> JSON String 언파싱 수행
//        String jsonValue;
//        //
//        boolean isSet = Boolean.TRUE.equals(
//            redisTemplateDb0.opsForValue().setIfAbsent(
//                key,
//                "",
//                // jsonValue,  // Product 객체를 JSON String 으로 저장
//                Duration.ofSeconds(300)  // 10분간 재사용 가능한 캐시 유지
//            )
//        );
//        if (isSet) {
//            // 캐시에서 받아온 값을 응답 (String 을 Product 로 파싱)
//            // 2) JSON String -> Object 파싱
//            String cachedProductString = redisTemplateDb0.opsForValue().get(key);
//            Product cachedProduct;
//            return Optional.of(
//                new Product()
//                // cachedProduct
//            );  // Product 객체 형태로 반환
//        }
//        // 현재 요청이 최초 요청이므로 바로 생성
//        return Optional.empty();
//    }
}
