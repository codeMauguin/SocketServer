package web.http.Filter;

import web.http.HttpRequest;
import web.http.HttpResponse;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 9:37 AM 2022/1/9
 * @Modified By:
 */
public interface FilterChain {
    void doFilter(HttpRequest request, HttpResponse response);
}
