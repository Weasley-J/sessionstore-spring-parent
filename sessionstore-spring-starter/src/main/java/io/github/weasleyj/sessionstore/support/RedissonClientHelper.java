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

    private RedissonClientHelper() {
    }

    /**
     * 获取指定Redis数据库的索引的Redisson客户端
     *
     * @param database Redis数据库的索引, 取值范围 [0,15]
     * @return 指定Redis数据库的索引的Redisson客户端
     * @apiNote 此方法创建的 RedissonClient 不受 Spring IOC 托管，使用完后需要关闭资源
     * @see RedissonClientHelper#shutdownRedissonClient(RedissonClient)
     * @see RedissonClient#shutdown()
     */
    public static RedissonClient createRedissonClient(int database) {
        Assert.isTrue(database >= 0 && database <= 15, "Database must be in [0,15].");
        RedisProperties redisProperties = getRedisProperties(database);
        return createRedissonInstance(redisProperties);
    }

    /**
     * 关闭 Redisson 客户端
     *
     * @param redissonClient The redisson client you want to shut down
     */
    public static void shutdownRedissonClient(RedissonClient redissonClient) {
        if (redissonClient != null) {
            redissonClient.shutdown();
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
    private static RedissonClient createRedissonInstance(RedisProperties redisProperties) {
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
