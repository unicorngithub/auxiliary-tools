package com.auxiliary.interfaces.log.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;

/**
 * @author Guo's
 */
@Data
@Accessors(chain = true)
public class AuxiliaryDocumentMethodDto {
    private String name;
    private Field field;
}
