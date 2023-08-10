# sessionstore-spring-parent

[![Maven Central](https://img.shields.io/maven-central/v/io.github.weasley-j/sessionstore-spring-starter)](https://search.maven.org/artifact/io.github.weasley-j/sessionstore-spring-starter)

> 一个用于将**特定类型的数据**和**业务数据**解耦并独立存储的`spring-boot-starter`，允许选择性地将数据存放到独立的`Redis`
> 数据库中，基于`Redisson`实现，可以充分利用Redis的0~15号索引数据库。



常见可解耦的数据类型：

- 邮件队列：
- 认证与授权数据：用户的角色、权限信息可以独立存储，以提供更高的安全性和可扩展性
- 用户配置数据：个性化设置、首选项、主题选择等数据
- 统计数据：程序的统计信息，访问量、用户活跃度
- 外部服务数据： 一些应用可能需要与外部服务进行数据交互，这些数据可以独立存储或通过外部服务的API进行管理



`spring-boot`适配情况: 

| spring-boot版本       | JDK版本        | 适配 |
|---------------------|--------------|----|
| `spring-boot 2.x.x` | `>= JDK 1.8` | ✅  |
| `spring-boot 3.x.x` | ` >= JDK 17` | ✅  |



# 1 快速开始

### 1.1 引入Maven依赖

```xml
<dependency>
    <groupId>io.github.weasley-j</groupId>
    <artifactId>sessionstore-spring-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 1.2 配置Redis连接信息

[元数配置类](https://github.com/Weasley-J/sessionstore-spring-parent/blob/main/sessionstore-spring-starter/src/main/java/io/github/weasleyj/sessionstore/config/SessionStoreRedisProperties.java#L20-L69)

```yaml
session:
  store:
    enable: on
    redis:
      database: 1
      ssl: off
      host: 192.168.31.140
      port: 6379
      password: 123456
```

### 1.3 启用sessionstore

创建配置类

```java
import io.github.weasleyj.sessionstore.EnableSessionStore;
import org.springframework.context.annotation.Configuration;

/**
 * Session Store Configuration
 */
@Configuration
@EnableSessionStore
public class SessionStoreConfig {
}
```

或者，将注解添加启动类上：

```java

@EnableSessionStore
@SpringBootApplication
public class SomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SomeApplication.class, args);
    }

}
```

### 1.4 使用示例

#### 1.4.1 使用配置文件中指定的`Redis`默认索引库

- 注入默认`sessionStoreRedissonClient`实例对象, e.g:

  ```java
  @Autowired
  private RedissonClient sessionStoreRedissonClient
  ```

- Session Store Demo Controller

```java
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
```



#### 1.4.2 使用`RedissonClientHelper#createRedissonClient(int)`创建指定索引库的客户端

> `RedissonClientHelper#createRedissonClient`创建的`RedissonClient`不受`Spring IOC`托管，使用完成后，使用完后需要关闭资源。

代码示例：

```java
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

/**
 * RedissonClientHelper Test
 */
@SpringBootTest
class RedissonClientHelperTest {

    static final String KEY_PREFIX = SessionStoreRedisProperties.REDIS_KEY_PREFIX;

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
        redissonClient.shutdown();
    }

    @Test
    @DisplayName("创建Client并指定Redis索引库，使用其他对象取出数据")
    void createRedissonClient2() {
        RedissonClient redissonClient = RedissonClientHelper.createRedissonClient(2);
        RBucket<UserDTO> bucket = redissonClient.getBucket(KEY_PREFIX + ":" + 1, CodecSupport.codec(UserDTO.class));
        System.out.println(bucket.get()); // UserDTO(uid=null, name=张三, age=22)
        Assertions.assertNotNull(bucket.get());
        redissonClient.shutdown();
    }

}
```

`User` 与 `UserDTO` 结构对比:

![image-20230807122436022](https://weasley.oss-cn-shanghai.aliyuncs.com/Photos/image-20230807122436022.png)
