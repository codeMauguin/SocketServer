package web.http.Libary;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class ControllerMethod {
    private final Method method;
    private final String path;
    private final String[] mapper;
    private final Parameter[] parameters;

    public ControllerMethod(Method method, String path, String[] mapper) {
        this.method = method;
        this.path = path;
        parameters = method.getParameters();
        this.mapper = mapper;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Parameter[] getParameters() {
        return parameters;
    }


    public boolean getMapper(String method) {
        return Arrays.stream(mapper).allMatch(str -> str.matches(method));
    }
}
