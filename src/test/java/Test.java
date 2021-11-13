import JSON.JSON;
import Logger.Logger;
import server.Server;
import web.Socket.HttpFactory;
import web.Socket.WebHttpServerFactory;
import web.annotation.WebServlet;
import web.http.Controller.Servlet;
import web.http.HttpRequest;
import web.http.HttpResponse;

import java.util.Arrays;
import java.util.List;

class u {
    int id ;

    public u(int id) {
        this.id = id;
    }
}

class pojo {
    int code = 200;
    String msg = "成功获取";
    int id = 1;
    String name = "陈浩";
    List<u> us = Arrays.asList(new u(1), new u(2));
}

/**
 * @author 陈浩
 * @Date: 21/06/02 0:18
 */
public class Test {
    private final static int PORT = 80;

    @SuppressWarnings("all")
    public static void main(String[] args) throws Throwable {
        Server<Integer> server = new HttpFactory();
        @WebServlet("/api/user/login")
        class login implements Servlet {
            @Override
            public void doGet(HttpRequest request, HttpResponse response) {
                response.getPrintSteam().println(JSON.ObjectToString(new pojo()));
            }

            @Override
            public void doPost(HttpRequest request, HttpResponse response) {
                doGet(request, response);
            }
        }
        final Servlet webServlet = new login();
        WebHttpServerFactory server1 = (WebHttpServerFactory) server;
        server1.getServletFactory().addContainer(webServlet);
        server1.addContainer((request, response) -> {
            Logger.info("进入过滤器");
            response.setCharset("UTF-8");
        });
        server.start(PORT);
    }
}
