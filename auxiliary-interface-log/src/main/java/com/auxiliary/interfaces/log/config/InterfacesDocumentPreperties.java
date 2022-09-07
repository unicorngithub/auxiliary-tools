package com.auxiliary.interfaces.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Guo's
 */
@Data
@ConfigurationProperties(prefix = "iguos.auxiliary.document")
public class InterfacesDocumentPreperties {
    private boolean enable = true;
    private boolean debug = false;
    private String packages;
    private String pattern = "/**/*Controller.class";
}
