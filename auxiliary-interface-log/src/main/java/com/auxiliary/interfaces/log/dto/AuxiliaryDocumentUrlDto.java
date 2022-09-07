package com.auxiliary.interfaces.log.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Guo's
 */
@Data
@Accessors(chain = true)
public class AuxiliaryDocumentUrlDto {
    private String url;
    private RequestMethod[] method;
}
