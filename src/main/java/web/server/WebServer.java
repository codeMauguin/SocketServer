package web.server;

import server.Server;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:05 下午 2021/11/29
 * @Modified By:
 */
public interface WebServer extends Server<WebServerContext> {
    void run(String[] args);
}
