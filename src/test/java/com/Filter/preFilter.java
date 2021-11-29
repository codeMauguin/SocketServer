package com.Filter;

import web.http.Filter.Filter;
import web.http.Filter.annotation.WebFilter;
import web.http.HttpRequest;
import web.http.HttpResponse;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 5:36 下午 2021/11/29
 * @Modified By:
 */
@WebFilter("/api")
public class preFilter implements Filter {
    @Override
    public void doFilter(HttpRequest request, HttpResponse response) {
        System.out.println("进入");
    }
}
