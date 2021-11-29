package web.http.Controller;

public class ClassRegistrar {
    public static <T> T registrar(Class<T> target) throws Throwable {
        return target.getDeclaredConstructor().newInstance();
    }
}
