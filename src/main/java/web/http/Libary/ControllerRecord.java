package web.http.Libary;

import web.http.Controller.Servlet;

import java.util.Set;

public class ControllerRecord {
    private final String regex;
    private final Object instance;
    private final Set<ControllerMethod> methods;

    private final boolean isServlet;

    public ControllerRecord(String regex, Object instance, Set<ControllerMethod> methods, boolean isServlet) {
        this.regex = regex;
        this.instance = instance;
        this.methods = methods;
        this.isServlet = isServlet;
    }

    public Servlet getServlet() {
        return (Servlet) instance;
    }

    public String getRegex() {
        return regex;
    }

    public Object getInstance() {
        return instance;
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
