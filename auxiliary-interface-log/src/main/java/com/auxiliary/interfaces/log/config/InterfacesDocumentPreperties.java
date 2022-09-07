package com.auxiliary.interfaces.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Guo's
 */
@Data
@ConfigurationProperties(prefix = "auxiliary.document")
public class InterfacesDocumentPreperties {
    /**
     * auxiliary.document.enable
     */
    private boolean enable = true;
    /**
     * auxiliary.document.debug
     */
    private boolean debug = false;
    /**
     * auxiliary.document.packages
     */
    private String packages = "";
    /**
     * auxiliary.document.pattern
     */
    private String pattern = "/**/*Controller.class";
}
