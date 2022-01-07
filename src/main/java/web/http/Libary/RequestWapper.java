package web.http.Libary;

import web.http.Controller.annotation.GetMapper;
import web.http.Controller.annotation.PostMapper;
import web.http.Controller.annotation.PutMapper;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:13 AM 2022/1/5
 * @Modified By:
 */
public enum RequestWapper {
    GET(GetMapper.class, RequestMethod.GET), POST(PostMapper.class, RequestMethod.POST), PUT(PutMapper.class, RequestMethod.PUT);

    private final RequestMethod method;
    private final Class<? extends Annotation> mapper;

    RequestWapper(Class<? extends Annotation> mapperClass, RequestMethod method) {
        this.mapper = mapperClass;
        this.method = method;
    }

    public static RequestMethod get(Class<? extends Annotation> annotation) {
        return Arrays.stream(RequestWapper.values()).filter(r -> r.getMapper().equals(annotation)).findFirst().map(RequestWapper::getMethod).orElse(null);
    }

    public Class<? extends Annotation> getMapper() {
        return mapper;
    }

    public RequestMethod getMethod() {
        return method;
    }
}
