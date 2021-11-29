package web.Socket;

import web.server.WebServerContext;

import java.net.Socket;

public class HttpFactory extends WebHttpServerFactory {

    @Override
    public void start(WebServerContext context) throws Throwable {
        super.start(context);
        /*
         *
         */
        while (start) {
            Socket accept = this.serverSocket.accept();
            executor.execute(new SockAccept(accept,  context));
        }
    }

    @Override
    public void destroy(WebServerContext context) {
        super.destroy(context);
        /*
         *
         */
    }
}
