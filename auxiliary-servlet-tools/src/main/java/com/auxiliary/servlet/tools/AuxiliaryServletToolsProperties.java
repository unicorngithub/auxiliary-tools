package com.auxiliary.servlet.tools;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Guo's
 */
@Data
@ConfigurationProperties(prefix = "iguos.auxiliary.servlet-tools")
public class AuxiliaryServletToolsProperties {

}
