package org.context.Bean.annotation;

import java.lang.annotation.*;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:52 下午 2021/11/30
 * @Modified By:
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {
    String value() default "";
}
