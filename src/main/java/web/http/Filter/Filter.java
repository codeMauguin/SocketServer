package web.http.Filter;

import web.http.HttpRequest;
import web.http.HttpResponse;

public interface Filter {
    void doFilter(HttpRequest request, HttpResponse response, FilterChain filterChain);
}
