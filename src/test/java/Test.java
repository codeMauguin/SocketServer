import JSON.JSON;
import Logger.Logger;
import web.Socket.HttpFactory;
import web.Socket.WebHttpServerFactory;
import web.annotation.WebServlet;
import web.http.Controller.Servlet;
import web.http.HttpRequest;
import web.http.HttpResponse;

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
        WebHttpServerFactory server = new HttpFactory();
        @WebServlet("/api/user/login")
        class login implements Servlet {
            @Override
            public void doGet(HttpRequest request, HttpResponse response) {
                System.out.println("进入处理");
                response.getPrintSteam().println(JSON.ObjectToString(new pojo(request.getParam("id"))));
            }

            @Override
            public void doPost(HttpRequest request, HttpResponse response) {
                doGet(request, response);
            }
        }
        final Servlet webServlet = new login();
        server.getServletFactory().addContainer(webServlet);
        server.addContainer((request, response) -> {
            Logger.info("进入过滤器");
            response.setCharset("UTF-8");
        });
        /**
         * 服务启动
         */
        server.start(PORT);
    }
}
