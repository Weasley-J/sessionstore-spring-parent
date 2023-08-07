package io.github.weasleyj.sessionstore.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * The session store redis properties
 *
 * @author weasley
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = SessionStoreRedisProperties.PREFIX)
public class SessionStoreRedisProperties {
    /**
     * 配置文件前缀
     */
    public static final String PREFIX = "session.store";
    /**
     * Session store Redis 的 key 前缀
     */
    public static final String REDIS_KEY_PREFIX = "session:store";
    /**
     * 是否启用
     */
    private boolean enable = true;
    /**
     * redis配置属性
     */
    @NestedConfigurationProperty
    private RedisProperties redis;

    /**
     * redis配置属性
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisProperties {
        /**
         * Whether to enable SSL support. use "rediss://" for SSL connection
         */
        private boolean ssl;
        /**
         * Redis host
         */
        private String host = "localhost";
        /**
         * Redis port
         */
        private int port = 6379;
        /**
         * Redis username
         */
        private String username;
        /**
         * Redis password
         */
        private String password;
        /**
         * Redis database
         */
        private Integer database = 0;
    }
}
