package web.http.annotation;

import java.lang.annotation.*;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:35 上午 2021/12/1
 * @Modified By:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Component {
    String value() default "";
}
