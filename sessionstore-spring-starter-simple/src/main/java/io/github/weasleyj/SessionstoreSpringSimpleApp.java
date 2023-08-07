package io.github.weasleyj;

import io.github.weasleyj.sessionstore.EnableSessionStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Session store 应用示例
 *
 * @author weasley
 * @version 1.0.0
 */
@EnableSessionStore
@SpringBootApplication
public class SessionstoreSpringSimpleApp {
    public static void main(String[] args) {
        SpringApplication.run(SessionstoreSpringSimpleApp.class, args);
    }

}
