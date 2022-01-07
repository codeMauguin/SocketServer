package com.controller;

import web.http.Controller.Servlet;
import web.http.Controller.annotation.WebServlet;
import web.http.HttpRequest;
import web.http.HttpResponse;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 12:17 PM 2022/1/6
 * @Modified By:
 */
@WebServlet("/servlet")
public class servletHello implements Servlet {
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.getPrintSteam().println("你好");
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        doGet(request, response);
    }
}
