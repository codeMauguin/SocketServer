package web.http.Controller.annotation;

import java.lang.annotation.*;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 3:13 下午 2021/12/1
 * @Modified By:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface RequestMapper {
    String value() default "/";
}
