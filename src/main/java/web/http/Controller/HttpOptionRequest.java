package web.http.Controller;

import web.Socket.Handle.HttpServletResponse;
import web.http.HttpRequest;
import web.server.WebServerContext;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 6:18 下午 2021/12/5
 * @Modified By:
 */
public class HttpOptionRequest {
    public static boolean handle(WebServerContext context, HttpRequest request, HttpServletResponse response) {
        if (response.getOrigin().equals(""))
            return true;
        boolean b = context.checkOrigin(response.getOrigin());
        if (b) {
            if (request.getMethod().equals("OPTIONS")) {
                response.addHeader("Access-Control-Allow-Methods", response.getMethod());
                response.addHeader("Access-Control-Allow-Headers", "content-type");
                response.addHeader("Access-Control-Max-Age", "1800");
                response.addHeader("Allow", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");
            }
            response.addHeader("Access-control-allow-credentials", "true");
            response.addHeader("Vary", "Origin");
            response.addHeader("Vary", "Access-Control-Request-Method");
            response.addHeader("Vary", "Access-Control-Request-Headers");
            response.addHeader("Access-Control-Allow-Origin", response.getOrigin());
        }
        return b;
    }
}
