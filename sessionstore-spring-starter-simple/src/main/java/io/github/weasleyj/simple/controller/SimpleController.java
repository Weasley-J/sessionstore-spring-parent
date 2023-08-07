package io.github.weasleyj.simple.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties;
import io.github.weasleyj.simple.dto.UserDTO;
import io.github.weasleyj.simple.entity.User;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static io.github.weasleyj.sessionstore.support.CodecSupport.codec;


/**
 * Session Store Demo Controller
 * <p>
 * 使用配置文件中指定的Redis默认索引库
 *
 * @author weasley
 * @version 1.0.0
 */
@RestController
@RequestMapping("/session/store/simple")
public class SimpleController {
    static final String KEY_PREFIX = SessionStoreRedisProperties.REDIS_KEY_PREFIX;

    @Autowired
    private RedissonClient sessionStoreRedissonClient;

    /**
     * 存入用户json
     */
    @PostMapping("/save")
    public User save() {
        User user = new User();
        RBucket<User> bucket = sessionStoreRedissonClient.getBucket(KEY_PREFIX + ":" + user.getId());
        if (bucket.isExists()) {
            return bucket.get();
        }
        bucket.set(user, Duration.ofMinutes(30));
        return bucket.get();
    }

    /**
     * 使用不同的DTO类型将用户数据取出
     */
    @PostMapping("/info1/{uid}")
    public UserDTO getUser1(@PathVariable("uid") Long uid) {
        RBucket<UserDTO> bucket = sessionStoreRedissonClient.getBucket(KEY_PREFIX + ":" + uid, codec(UserDTO.class));
        return bucket.get();
    }

    /**
     * 使用不同的DTO类型将用户数据取出
     */
    @PostMapping("/info2/{uid}")
    public UserDTO getUser2(@PathVariable("uid") Long uid) {
        RBucket<UserDTO> bucket = sessionStoreRedissonClient.getBucket(KEY_PREFIX + ":" + uid, codec(new TypeReference<UserDTO>() {
        }));
        return bucket.get();
    }
}

