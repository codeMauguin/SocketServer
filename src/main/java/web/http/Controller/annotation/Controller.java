package web.http.Controller.annotation;

import web.http.annotation.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Controller {
    String value() default "";
}
