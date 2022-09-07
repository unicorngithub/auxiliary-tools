
package com.auxiliary.interfaces.log.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;

@Data
@Accessors(chain = true)
public class AuxiliaryDocumentBeanDto {
    private String className;
    private String beanName;
    private Method method;
    private Class clas;
}