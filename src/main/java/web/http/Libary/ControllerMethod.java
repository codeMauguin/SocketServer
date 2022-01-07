package web.http.Libary;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public class ControllerMethod {
    private final Method method;
    private final String path;
    private final String[] mapper;
    private final Parameter[] parameters;

    public ControllerMethod(Method method, String path, RequestMethod... mapper) {
        this.method = method;
        this.path = path;
        parameters = method.getParameters();
        this.mapper = Arrays.stream(mapper).map(Enum::name).toArray(String[]::new);
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
        return Arrays.stream(mapper).anyMatch(str -> Pattern.matches(method, str));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControllerMethod that = (ControllerMethod) o;
        return Objects.equals(method, that.method) && path.equals(that.path) && Arrays.equals(mapper, that.mapper) && Arrays.equals(parameters,
                that.parameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method, path);
        result = 31 * result + Arrays.hashCode(mapper);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }
}
