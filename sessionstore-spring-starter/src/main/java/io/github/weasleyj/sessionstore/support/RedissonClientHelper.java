package io.github.weasleyj.sessionstore.support;

import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties;
import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties.RedisProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.util.Assert;

/**
 * Redisson Client Helper
 *
 * @author weasley
 * @since 1.0.0
 */
public final class RedissonClientHelper {
    private static volatile RedissonClient redissonClientInstance;

    private RedissonClientHelper() {
    }

    /**
     * 获取指定Redis数据库的索引的Redisson客户端
     *
     * @param database Redis数据库的索引, 取值范围 [0,15]
     * @return 指定Redis数据库的索引的Redisson客户端
     * @apiNote 此方法创建的 RedissonClient 是线程安全的，并且不受 Spring IOC 托管，使用完后接的调用 shutdownRedissonClient 释放资源
     * @see RedissonClientHelper#shutdownRedissonClient()
     * @see SessionStoreRedisProperties
     */
    public static RedissonClient createRedissonClient(int database) {
        Assert.isTrue(database >= 0 && database <= 15, "Database must be in [0,15].");
        RedissonClient redissonClient = redissonClientInstance;
        if (redissonClient == null) {
            synchronized (RedissonClientHelper.class) {
                redissonClient = redissonClientInstance;
                if (redissonClient == null) {
                    RedisProperties redisProperties = getRedisProperties(database);
                    redissonClientInstance = createRedissonClient(redisProperties);
                }
            }
        }
        return redissonClientInstance;
    }

    /**
     * 关闭 Redisson 客户端
     */
    public static void shutdownRedissonClient() {
        if (redissonClientInstance != null) {
            redissonClientInstance.shutdown();
        }
    }

    /**
     * 将配置文件中Redis的属性复制一份
     *
     * @param database Redis数据库的索引, 取值范围 [0,15]
     * @return RedisProperties
     */
    private static RedisProperties getRedisProperties(int database) {
        SessionStoreRedisProperties sessionStoreRedisProperties = SessionStoreApplicationContextProvider.getBean(SessionStoreRedisProperties.class);
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setDatabase(database);
        redisProperties.setSsl(sessionStoreRedisProperties.getRedis().isSsl());
        redisProperties.setHost(sessionStoreRedisProperties.getRedis().getHost());
        redisProperties.setPort(sessionStoreRedisProperties.getRedis().getPort());
        redisProperties.setUsername(sessionStoreRedisProperties.getRedis().getUsername());
        redisProperties.setPassword(sessionStoreRedisProperties.getRedis().getPassword());
        return redisProperties;
    }

    /**
     * 返回一个RedissonClient
     *
     * @param redisProperties redis配置属数据
     * @return RedissonClient
     */
    private static RedissonClient createRedissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        SingleServerConfig singleServer = config.useSingleServer()
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase());
        if (redisProperties.isSsl()) {
            singleServer.setAddress("rediss://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        } else {
            singleServer.setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        }
        return Redisson.create(config);
    }
}
