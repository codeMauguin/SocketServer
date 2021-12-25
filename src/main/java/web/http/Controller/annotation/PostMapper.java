package web.http.Controller.annotation;

import web.http.Libary.RequestMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface PostMapper {
    RequestMethod METHOD = RequestMethod.POST;

    String value() default "";
}
