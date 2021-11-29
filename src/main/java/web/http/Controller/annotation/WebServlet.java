package web.http.Controller.annotation;

import java.lang.annotation.*;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 22:38 2021/11/13
 * @Modified By: 陈浩
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface WebServlet {
    String value() default "/";
}
