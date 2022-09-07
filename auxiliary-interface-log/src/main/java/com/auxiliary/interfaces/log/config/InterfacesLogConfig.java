package com.auxiliary.interfaces.log.config;

import com.auxiliary.interfaces.log.advice.InterfacesDocumentAdvice;
import com.auxiliary.interfaces.log.advice.InterfacesRunLogAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 辅助工具-启动打印日志
 *
 * @author Guo's
 */
@EnableAsync // 启用异步支持
@EnableConfigurationProperties({AuxiliaryInterfacesLogProperties.class, InterfacesDocumentPreperties.class})
public class InterfacesLogConfig {

    @Bean
    @Async
    @ConditionalOnMissingBean(InterfacesRunLogAdvice.class)
    public InterfacesRunLogAdvice auxiliaryInterfaceLogAdivce(AuxiliaryInterfacesLogProperties properties, ApplicationContext applicationContext) {
        if (properties.isEnable()) {
            InterfacesRunLogAdvice auxiliaryInterfaceLogAdivce = new InterfacesRunLogAdvice(properties, applicationContext);
            try {
                Thread.sleep(1000 * 1);
                auxiliaryInterfaceLogAdivce.printInterface();
            } catch (Exception ex) {
            }
            return auxiliaryInterfaceLogAdivce;
        }
        return null;
    }

    @Bean
    @Async
    @ConditionalOnMissingBean(InterfacesDocumentPreperties.class)
    public InterfacesDocumentAdvice interfacesDocumentAdvice(InterfacesDocumentPreperties properties, ApplicationContext applicationContext) {
        if (properties.isEnable()) {
            InterfacesDocumentAdvice auxiliaryInterfaceLogAdivce = new InterfacesDocumentAdvice(properties, applicationContext);
            try {
                Thread.sleep(1100 * 1);
                auxiliaryInterfaceLogAdivce.printDocument();
            } catch (Exception ex) {
            }
            return auxiliaryInterfaceLogAdivce;
        }
        return null;
    }

}
