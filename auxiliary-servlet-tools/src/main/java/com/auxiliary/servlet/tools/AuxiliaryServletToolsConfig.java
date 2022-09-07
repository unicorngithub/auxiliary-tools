package com.auxiliary.servlet.tools;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

/**
 * @author Guo's
 */
@EnableConfigurationProperties(AuxiliaryServletToolsProperties.class)
public class AuxiliaryServletToolsConfig {

    private final AuxiliaryServletToolsProperties properties;

    public AuxiliaryServletToolsConfig(AuxiliaryServletToolsProperties properties, ApplicationContext application) {
        this.properties = properties;
    }

}
