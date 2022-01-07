package web.http.Controller;

import org.context.Bean.DefaultSingletonBeanRegistry;
import org.reflections.Reflections;
import web.http.Controller.annotation.*;
import web.http.Filter.Filter;
import web.http.Filter.FilterRecord;
import web.http.Filter.annotation.Order;
import web.http.Filter.annotation.WebFilter;
import web.http.Libary.ControllerMethod;
import web.http.Libary.ControllerRecord;
import web.http.Libary.RequestMethod;
import web.http.Libary.RequestWapper;
import web.http.annotation.Component;
import web.http.annotation.Service;
import web.server.WebServerContext;
import web.util.ArraysUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class UtilScan {
    private static final String prefix = "/";

    public static Set<ControllerRecord> scanController(Reflections reflections, WebServerContext context) throws Throwable {
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        Set<ControllerRecord> controllerRecords = new HashSet<>();
        for (Class<?> controller : controllers) {
            String prefix = "/";
            if (controller.isAnnotationPresent(RequestMapper.class))
                prefix = controller.getAnnotation(RequestMapper.class).value().trim();
            prefix = pathProcess(prefix, "");
            Method[] declaredMethods = controller.getDeclaredMethods();
            Set<ControllerMethod> controllerMethods = new HashSet<>();
            List<Class<? extends Annotation>> list = Arrays.asList(GetMapper.class, PostMapper.class, PutMapper.class);
            for (Method method : declaredMethods) {
                Class<? extends Annotation> first = ArraysUtil.findFirst(list, method::isAnnotationPresent);
                if (first != null) {
                    extracted(controllerMethods, method, method.getAnnotation(first));
                } else if (method.isAnnotationPresent(RequestMapper.class)) {
                    RequestMapper mapper = method.getAnnotation(RequestMapper.class);
                    String suffix = Pattern.matches("^/.*", mapper.value()) ? mapper.value().substring(1) : mapper.value();
                    controllerMethods.add(new ControllerMethod(method, suffix, mapper.methods()));
                }
            }
            controllerRecords.add(new ControllerRecord(prefix, context, controllerMethods, controller, false));
        }
        return controllerRecords;
    }

    private static void extracted(Set<ControllerMethod> controllerMethods, Method method, Annotation mapper) throws Throwable {
        Method MOD = mapper.annotationType().getMethod("value");
        String value = (String) MOD.invoke(mapper);
        String suffix = Pattern.matches("^/.*", value) ? value.substring(1) : value;
        RequestMethod METHOD = RequestWapper.get(mapper.annotationType());
        controllerMethods.add(new ControllerMethod(method, suffix, METHOD));
    }

    public static String pathProcess(String route, String suffix) {
        if (route.equals("/") || Pattern.matches("^/.*/$", route)) {
            route = route.concat(suffix);
        } else if (Pattern.matches("^(?!/).*/$", route)) {
            route = UtilScan.prefix.concat(route).concat(suffix);
        } else if (Pattern.matches("^/.*(?<!/)$", route)) {
            route = route.concat(prefix).concat(suffix);
        } else {
            route = UtilScan.prefix.concat(route).concat(UtilScan.prefix).concat(suffix);
        }
        return route;
    }

    public static Set<FilterRecord> scanFilter(Reflections route) throws Throwable {
        Set<FilterRecord> filters = new TreeSet<>();
        Set<Class<?>> typesAnnotatedWith = route.getTypesAnnotatedWith(WebFilter.class);
        for (Class<?> filterScan : typesAnnotatedWith) {
            if (!Filter.class.isAssignableFrom(filterScan)) {
                continue;
            }
            Filter registrar = (Filter) ClassRegistrar.registrar(filterScan);
            WebFilter webFilter = filterScan.getAnnotation(WebFilter.class);
            Order order = filterScan.isAnnotationPresent(Order.class) ? filterScan.getAnnotation(Order.class) : null;
            int index = order == null ? 1 : order.value();
            filters.add(new FilterRecord(webFilter.value(), registrar, index));// /api/.*
        }
        return filters;
    }

    public static Set<ControllerRecord> scanServlet(Reflections route) throws Throwable {
        Set<ControllerRecord> records = new HashSet<>();
        Set<Class<?>> typesAnnotatedWith = route.getTypesAnnotatedWith(WebServlet.class);
        for (Class<?> servlets : typesAnnotatedWith) {
            Object registrar = ClassRegistrar.registrar(servlets);
            WebServlet annotation = servlets.getAnnotation(WebServlet.class);
            String pathProcess = pathProcess(annotation.value(), "");
            pathProcess = pathProcess.substring(0, pathProcess.length() - 1);
            records.add(new ControllerRecord(pathProcess, registrar, null, null, true));
        }
        return records;
    }

    public static void prepareBean(Set<Reflections> reflections, DefaultSingletonBeanRegistry registry) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Reflections reflection1 : reflections) {
            registryByAnnotation(reflection1.getTypesAnnotatedWith(Controller.class), Controller.class, registry);
            registryByAnnotation(reflection1.getTypesAnnotatedWith(Service.class), Service.class, registry);

            registryByAnnotation(reflection1.getTypesAnnotatedWith(Component.class), Component.class, registry);
        }
    }

    private static void registryByAnnotation(Set<Class<?>> target, Class<? extends Annotation> annotation, DefaultSingletonBeanRegistry registry) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Class<?> aClass : target) {
            Annotation annoy = aClass.getAnnotation(annotation);
            registry.registered(aClass, nameHandle(aClass, annoy));
        }
    }

    private static String defaultHandle(Class<?> target) {
        String simpleName = target.getSimpleName();
        return simpleName.substring(0, 1).toLowerCase(Locale.ROOT).concat(simpleName.substring(1));
    }

    private static String nameHandle(Class<?> target, Annotation annotation) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method var0 = annotation.annotationType().getMethod("value");
        String value = (String) var0.invoke(annotation);
        return value.equals("") ? defaultHandle(target) : value;
    }
}
