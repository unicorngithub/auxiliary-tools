package com.auxiliary.interfaces.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "iguos.auxiliary.interface-log")
public class AuxiliaryInterfacesLogProperties {

    private boolean enable = true;

}
