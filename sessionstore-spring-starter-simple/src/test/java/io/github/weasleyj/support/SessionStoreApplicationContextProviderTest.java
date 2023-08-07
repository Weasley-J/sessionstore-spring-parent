package io.github.weasleyj.support;

import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties;
import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties.RedisProperties;
import io.github.weasleyj.sessionstore.support.SessionStoreApplicationContextProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SessionStoreApplicationContextProviderTest {

    private static RedisProperties getRedisProperties() {
        SessionStoreRedisProperties storeRedisProperties = SessionStoreApplicationContextProvider.getBean(SessionStoreRedisProperties.class);
        return storeRedisProperties.getRedis();
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getBean() {
        RedisProperties redisProperties = getRedisProperties();
        System.out.println(redisProperties);
        Assertions.assertNotNull(redisProperties, "redisProperties not null");
    }
}
