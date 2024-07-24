package ac.su.kdt.redistrcontrol.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisServiceTest {
    @Autowired
    RedisService redisService;

    @Test
    void set() {
        assert redisService.setIfAbsent(
            // 일정 시간 내에 중복 키가 redis 에 입력될 수 없도록 보장
            "hahahaha", "this is test!"
        );
    }
}