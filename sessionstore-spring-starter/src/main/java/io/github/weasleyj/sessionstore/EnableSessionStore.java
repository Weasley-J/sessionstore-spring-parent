package io.github.weasleyj.sessionstore;

import io.github.weasleyj.sessionstore.config.SessionStoreConfiguration;
import io.github.weasleyj.sessionstore.support.SessionStoreApplicationContextProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to enable Session Store
 *
 * @author weasley
 * @version 1.0.0
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SessionStoreConfiguration.class, SessionStoreApplicationContextProvider.class})
public @interface EnableSessionStore {
}
