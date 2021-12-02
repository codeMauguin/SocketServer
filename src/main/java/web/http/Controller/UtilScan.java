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
import web.http.annotation.Component;
import web.http.annotation.Service;
import web.server.WebServerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class UtilScan {
    private static final String prefix = "/";

    public static Set<ControllerRecord> scanController(Reflections reflections, WebServerContext context) {
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        Set<ControllerRecord> controllerRecords = new HashSet<>();
        for (Class<?> controller : controllers) {
//            Object registrar = ClassRegistrar.registrar(controller);
            Object registrar = context.getBean(controller);
            String prefix = "/";
            if (controller.isAnnotationPresent(RequestMapper.class))
                prefix = controller.getAnnotation(RequestMapper.class).value().trim();
            prefix = pathProcess(prefix, "");
            Method[] declaredMethods = controller.getDeclaredMethods();
            Set<ControllerMethod> controllerMethods = new HashSet<>();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(GetMapper.class)) {
                    GetMapper mapper = method.getAnnotation(GetMapper.class);
                    String suffix = mapper.value().matches("^/.*") ? mapper.value().substring(1) : mapper.value();
                    controllerMethods.add(new ControllerMethod(method, suffix, new String[]{"GET"}));
                } else if (method.isAnnotationPresent(PostMapper.class)) {
                    PostMapper mapper = method.getAnnotation(PostMapper.class);
                    String suffix = mapper.value().matches("^/.*") ? mapper.value().substring(1) : mapper.value();
                    controllerMethods.add(new ControllerMethod(method, suffix, new String[]{"POST"}));
                }
            }
            controllerRecords.add(new ControllerRecord(prefix, registrar, controllerMethods, false));
        }
        return controllerRecords;
    }

    public static String pathProcess(String route, String suffix) {
        if (route.equals("/") || route.matches("^/.*/$")) {
            route = route.concat(suffix);
        } else if (route.matches("^(?!/).*/$")) {
            route = UtilScan.prefix.concat(route).concat(suffix);
        } else if (route.matches("^/.*(?<!/)$")) {
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
            records.add(new ControllerRecord(pathProcess, registrar, null, true));
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

    private static void registryByAnnotation(Set<Class<?>> target,
                                             Class<? extends Annotation> annotation, DefaultSingletonBeanRegistry registry) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
