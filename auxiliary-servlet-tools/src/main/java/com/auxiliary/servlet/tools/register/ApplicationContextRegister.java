package com.auxiliary.servlet.tools.register;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 使用ApplicationContextAware的方式获取ApplicationContext，对非web及web环境都有很好的支持
 */
@Component
@Lazy(false)
@Order(1)
public class ApplicationContextRegister {

    private static ApplicationContext applicationContext;

    /**
     * 设置Spring上下文
     *
     * @param applicationContext
     * @throws BeansException
     */
    public ApplicationContextRegister(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}