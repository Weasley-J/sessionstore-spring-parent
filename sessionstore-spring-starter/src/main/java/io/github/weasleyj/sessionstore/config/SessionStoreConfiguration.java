package io.github.weasleyj.sessionstore.config;

import io.github.weasleyj.sessionstore.EnableSessionStore;
import io.github.weasleyj.sessionstore.Version;
import io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties.RedisProperties;
import io.github.weasleyj.sessionstore.support.CodecSupport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

import java.io.Serializable;

import static io.github.weasleyj.sessionstore.config.SessionStoreRedisProperties.PREFIX;

/**
 * Session store redisson auto configure
 *
 * @author weasley
 * @version 1.0.0
 * @see CodecSupport
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({EnableSessionStore.class})
@EnableConfigurationProperties({SessionStoreRedisProperties.class})
@ConditionalOnProperty(prefix = PREFIX, value = {"enable"}, havingValue = "true")
public class SessionStoreConfiguration {
    /**
     * The client name(Bean name of client)
     */
    public static final String CLIENT_NAME = "sessionStoreRedissonClient";

    /**
     * @return RedissonClient for store session
     */
    @Bean({CLIENT_NAME})
    @ConditionalOnMissingBean(value = {RedissonClient.class}, name = {CLIENT_NAME})
    public RedissonClient sessionStoreRedissonClient(SessionStoreRedisProperties sessionStoreRedisProperties) {
        RedisProperties redisProperties = sessionStoreRedisProperties.getRedis();
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        SingleServerConfig singleServer = config.useSingleServer().setDatabase(redisProperties.getDatabase());
        if (StringUtils.hasText(redisProperties.getUsername())) {
            singleServer.setUsername(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            singleServer.setPassword(redisProperties.getPassword());
        }
        if (redisProperties.isSsl()) {
            singleServer.setAddress("rediss://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        } else {
            singleServer.setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        }
        String version = Version.getVersion();
        if (StringUtils.hasText(version)) {
            log.info("Session store version: {}", version);
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean({RedisVersion.class})
    public RedisVersion redisVersion(SessionStoreRedisProperties sessionStoreRedisProperties) {
        RedisVersion version = new RedisVersion();
        SessionStoreRedisProperties.RedisProperties redisProperties = sessionStoreRedisProperties.getRedis();
        try (Jedis jedis = new Jedis(new HostAndPort(redisProperties.getHost(), redisProperties.getPort()), new SessionStoreJedisClientConfig(redisProperties))) {
            String server = jedis.info("server");
            if (StringUtils.hasText(server)) {
                String[] serverInfoArray = server.split("\r\n");
                for (String serverInfo : serverInfoArray) {
                    if ("redis_version".equals(serverInfo.split(":")[0])) {
                        version.setVersion(serverInfo.split(":")[1]);
                        version.setIntVersion(Integer.parseInt(version.getVersion().split("\\.")[0]));
                        break;
                    }
                }
            }
        }
        log.info("Redis version: {}", version.getVersion());
        return version;
    }

    /**
     * Request Restrict Jedis Client Config
     *
     * @author liuwenjing
     * @version 1.0.0
     */
    @Data
    private static class SessionStoreJedisClientConfig implements JedisClientConfig {
        /**
         * The properties of redis
         */
        private final SessionStoreRedisProperties.RedisProperties redis;

        SessionStoreJedisClientConfig(SessionStoreRedisProperties.RedisProperties redis) {
            this.redis = redis;
        }

        @Override
        public String getUser() {
            return redis.getUsername();
        }

        @Override
        public String getPassword() {
            return redis.getPassword();
        }

        @Override
        public int getDatabase() {
            return redis.getDatabase();
        }

        @Override
        public boolean isSsl() {
            return redis.isSsl();
        }
    }

    /**
     * Redis Version
     *
     * @author liuwenjing
     * @version 1.0.0
     */
    @Data
    public static class RedisVersion implements Serializable {
        /**
         * Redis version，i.e: 7.0.5
         */
        private String version;
        /**
         * Redis integer version，i.e: 7
         */
        private int intVersion;
    }
}
