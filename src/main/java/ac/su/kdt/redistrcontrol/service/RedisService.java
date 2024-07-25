package ac.su.kdt.redistrcontrol.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
}
