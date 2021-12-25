package web.http.Controller.annotation;

import web.http.Libary.RequestMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface GetMapper {
    RequestMethod METHOD = RequestMethod.GET;

    String value() default "/";
}
