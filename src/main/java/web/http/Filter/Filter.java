package web.http.Filter;

import web.http.HttpRequest;
import web.http.HttpResponse;

@FunctionalInterface
public interface Filter {
    void doFilter(HttpRequest request, HttpResponse response);
}
