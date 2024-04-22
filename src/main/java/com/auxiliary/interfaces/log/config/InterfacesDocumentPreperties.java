package com.auxiliary.interfaces.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 解析接口上加@AuxiliaryAnalysisDocument
 *
 * @author Guo's
 */
@Data
@ConfigurationProperties(prefix = "auxiliary.document")
public class InterfacesDocumentPreperties {
    /**
     * 是否开启插件
     * auxiliary.document.enable
     */
    private boolean enable = true;
    /**
     * 是否开启Debug模式
     * auxiliary.document.debug
     */
    private boolean debug = false;
    /**
     * 执行扫描路径
     * auxiliary.document.packages
     * classpath*:packages/pattern
     */
    private String packages = "";
    /**
     * 日志类型 0/1:注解指定/路径全部
     * auxiliary.document.printType
     */
    private int printType = 0;
    /**
     * auxiliary.document.pattern
     * classpath*:packages/pattern
     */
    private String pattern = "/**/**.class";
}
