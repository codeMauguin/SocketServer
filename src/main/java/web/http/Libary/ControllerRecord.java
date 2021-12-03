package web.http.Libary;

import web.http.Controller.Servlet;
import web.server.WebServerContext;

import java.util.Set;

public class ControllerRecord {
    private final String regex;
    private final Class<?> target;
    private final Set<ControllerMethod> methods;
    private final boolean isServlet;
    private WebServerContext context;
    private Servlet servlet;

    public ControllerRecord(String regex, Object context, Set<ControllerMethod> methods, Class<?> target,
                            boolean isServlet) {
        this.regex = regex;
        if (isServlet)
            this.servlet = (Servlet) context;
        else
            this.context = (WebServerContext) context;
        this.methods = methods;
        this.target = target;
        this.isServlet = isServlet;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public String getRegex() {
        return regex;
    }

    public Object getInstance() {
        return context.getBean(target);
    }

    public ControllerMethod getMethod(String path) {
        return methods.stream()
                .filter(method -> this.getRegex().concat(method.getPath()).equals(path))
                .findFirst()
                .orElse(null);
    }

    public boolean isServlet() {
        return isServlet;
    }
}
