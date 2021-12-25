package web.http.Controller;

import java.lang.reflect.Constructor;

public class ClassRegistrar {
    public static <T> T registrar(Class<T> target) throws Exception {
        return constructor(target).newInstance();
    }

    private static <T> Constructor<T> constructor(Class<T> target, Class<?>... parameters) throws Exception {
        Constructor<T> declaredConstructor = target.getDeclaredConstructor(parameters);
        declaredConstructor.setAccessible(true);
        return declaredConstructor;
    }

    public static <T> T registrar(Class<T> target, Class<?>[] parameters, Object... args) throws Exception {
        return constructor(target, parameters).newInstance(args);
    }
}
