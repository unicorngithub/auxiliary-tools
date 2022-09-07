package com.auxiliary.interfaces.log.interfaces;

import java.lang.annotation.*;

/**
 * @author Guo's
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AnalysisDocument {
    String value() default "";
}
