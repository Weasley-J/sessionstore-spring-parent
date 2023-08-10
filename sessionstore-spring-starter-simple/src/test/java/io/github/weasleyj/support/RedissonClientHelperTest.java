package io.github.weasleyj.support;

import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties;
import io.github.weasleyj.sessionstore.support.CodecSupport;
import io.github.weasleyj.sessionstore.support.RedissonClientHelper;
import io.github.weasleyj.simple.dto.UserDTO;
import io.github.weasleyj.simple.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * RedissonClientHelper Test
 */
@SpringBootTest
class RedissonClientHelperTest {

    static final String KEY_PREFIX = SessionStoreRedisProperties.REDIS_KEY_PREFIX;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("资源抢占测试: 并发创建指定索引库的RedissonClient并存入数据")
    void createRedissonClientConcurrency() throws InterruptedException {
        for (int i = 0; i <= 15; i++) {
            int finalI = i;
            new Thread(() -> {
                RedissonClient redissonClient = RedissonClientHelper.createRedissonClient(finalI);
                RBucket<User> bucket = redissonClient.getBucket(KEY_PREFIX + ":" + 1, CodecSupport.codec(User.class));
                bucket.set(new User(), Duration.ofMinutes(30));
                System.out.println(Thread.currentThread().getName() + ": " + bucket.get());
                Assertions.assertNotNull(bucket.get());
                redissonClient.shutdown();
            }).start();
        }
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    @DisplayName("创建Client并指定Redis索引库")
    void createRedissonClient() {
        RedissonClient redissonClient = RedissonClientHelper.createRedissonClient(2);
        User user = new User();
        RBucket<User> bucket = redissonClient.getBucket(KEY_PREFIX + ":" + user.getId());
        if (bucket.isExists()) {
            user = bucket.get();
        }
        bucket.set(user, Duration.ofMinutes(30));
        Assertions.assertNotNull(user);
        System.out.println(user); // User(id=1, name=张三, age=22)
        RedissonClientHelper.shutdownRedissonClient(redissonClient);
    }

    @Test
    @DisplayName("创建Client并指定Redis索引库，使用其他对象取出数据")
    void createRedissonClient2() {
        RedissonClient redissonClient = RedissonClientHelper.createRedissonClient(2);
        RBucket<UserDTO> bucket = redissonClient.getBucket(KEY_PREFIX + ":" + 1, CodecSupport.codec(UserDTO.class));
        System.out.println(bucket.get()); // UserDTO(uid=null, name=张三, age=22)
        Assertions.assertNotNull(bucket.get());
        RedissonClientHelper.shutdownRedissonClient(redissonClient);
    }

}
