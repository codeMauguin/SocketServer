package web.Socket;

import Logger.Logger;
import web.Socket.Handle.BioHttpHandle;
import web.server.WebServerContext;

import java.io.IOException;
import java.net.*;

public class HttpBioServer extends WebHttpServerFactory {
    private ServerSocket serverSocket;

    @Override
    public void start(WebServerContext context) throws Throwable {
        super.start(context);
        initServer(context);
        long end = System.currentTimeMillis();
        Logger.info("The program is running and starting up:{0}ms", end - context.getStart());
        /*
         *
         */
        while (this.start) {
            Socket accept = this.serverSocket.accept();
            executor.execute(new BioHttpHandle(accept, context));
        }
    }

    private void initServer(WebServerContext context) throws IOException {
        serverSocket = new ServerSocket();
        try {
            serverSocket.bind(new InetSocketAddress(context.getIp(), context.getPort()));
        } catch (BindException e) {
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), context.getPort()));
        }
        Logger.info("http Server start in port " + context.getPort());

    }

    @Override
    public void destroy(WebServerContext context) throws IOException {
        serverSocket.close();
        super.destroy(context);
        Logger.info("Service stops on port ".concat(String.valueOf(context.getPort())));
    }
}
