package io.github.weasleyj.sessionstore.support;

import io.github.weasleyj.sessionstore.EnableSessionStore;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * The Session Store ApplicationContext Provider
 *
 * @author weasley
 * @version 1.0.0
 */
@Component
@ConditionalOnClass({EnableSessionStore.class})
public class SessionStoreApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }

    @Override
    @SuppressWarnings({"all"})
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SessionStoreApplicationContextProvider.applicationContext = applicationContext;
    }
}
