package web.http.Libary;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class ControllerMethod {
    private final Method method;
    private final String path;
    private final String[] mapper;
    private final Parameter[] parameters;
    private String[] paramNames;


    public ControllerMethod(Method method, String path, String[] mapper) {
        this.method = method;
        this.path = path;
        parameters = method.getParameters();
        this.mapper = mapper;
        init();
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

    public String[] getParamNames() {
        return paramNames;
    }

    private void init() {
        paramNames = new String[parameters.length];
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = parameters[i].getName();
        }
    }

    public boolean getMapper(String method) {
        return Arrays.stream(mapper).allMatch(str -> str.matches(method));
    }
}
