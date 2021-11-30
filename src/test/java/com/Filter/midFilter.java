package com.Filter;

import web.http.Filter.Filter;
import web.http.Filter.annotation.Order;
import web.http.Filter.annotation.WebFilter;
import web.http.HttpRequest;
import web.http.HttpResponse;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 9:49 下午 2021/11/29
 * @Modified By:
 */
@WebFilter("/api")
@Order(10)
public class midFilter implements Filter {
    @Override
    public void doFilter(HttpRequest request, HttpResponse response) {
        System.out.println("10-进入");
    }
}
