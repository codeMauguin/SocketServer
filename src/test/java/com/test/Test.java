package com.test;

import web.server.WebServer;
import web.server.WebWorkServer;

import java.util.Arrays;
import java.util.List;

class u {
    int id;

    public u(int id) {
        this.id = id;
    }
}

class pojo {
    int code = 200;
    String msg = "成功获取";
    Object id;
    String name;
    List<u> us = Arrays.asList(new u(1), new u(2));

    public pojo(Object id) {
        this.id = id;
        name = "陈浩-" + id;
    }
}

/**
 * @author 陈浩
 * @Date: 21/06/02 0:18
 */
public class Test {
    private final static int PORT = 80;

    @SuppressWarnings("all")
    public static void main(String[] args) throws Throwable {
        WebServer webServer = new WebWorkServer();
        webServer.run(new String[]{"127.0.0.1:80", "com"});
    }
}
