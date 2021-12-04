package web.Socket;

import Logger.Logger;
import web.server.WebServerContext;

import java.io.IOException;
import java.net.Socket;

public class HttpFactory extends WebHttpServerFactory {

    @Override
    public void start(WebServerContext context) throws Throwable {
        super.start(context);
        long end = System.currentTimeMillis();
        Logger.info("The program is running and starting up:{0}ms", end - context.getStart());
        /*
         *
         */
        while (this.start) {
            Socket accept = this.serverSocket.accept();
            executor.execute(new SockAccept(accept, context));
        }
    }

    @Override
    public void destroy(WebServerContext context) throws IOException {
        super.destroy(context);
        serverSocket.close();
        Logger.info("Service stops on port ".concat(String.valueOf(context.getPort())));
    }
}
