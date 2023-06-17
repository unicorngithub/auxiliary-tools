package com.auxiliary.interfaces.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auxiliary.interface-log")
public class AuxiliaryInterfacesLogProperties {

    /**
     * auxiliary.interface-log.enable
     */
    private boolean enable = true;

}
