package com.test;

import web.server.WebServer;
import web.server.WebWorkServer;


/**
 * @author 陈浩
 * @Date: 21/06/02 0:18
 */
public class Test {

    public static void main(String[] args) {
        WebServer webServer = new WebWorkServer();
        webServer.run(args);
    }
}
