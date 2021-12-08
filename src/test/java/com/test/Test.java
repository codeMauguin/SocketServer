package com.test;

import web.server.WebServer;
import web.server.WebWorkServer;


/**
 * @author 陈浩
 * @Date: 21/06/02 0:18
 */
public class Test {

    public static void main(String[] args) {
        String buffer = "1290";
        int res = 0;
        int dest = 0;
        int pos = 0;
        while (pos < buffer.length()) {
            dest = buffer.charAt(pos++) - 48;
            res -= dest;

            res *= 10;
        }
        System.out.println(res);
        WebServer webServer = new WebWorkServer();
        webServer.run(args);
    }
}
