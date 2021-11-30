package web.Socket;

import Logger.Logger;
import server.Server;
import web.server.WebServerContext;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class WebHttpServerFactory implements Server<WebServerContext> {
    protected ThreadPoolExecutor executor;

    protected ServerSocket serverSocket;
    protected boolean start;


    @Override
    public void start(WebServerContext context) throws Throwable {
        executor = getExecutor();
        serverSocket = new ServerSocket();
        try {
            serverSocket.bind(new InetSocketAddress(context.getIp(), context.getPort()));
        } catch (BindException e) {
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), context.getPort()));
        }
        Logger.info("http Server start in port " + context.getPort());
        start = true;
        Thread hook = new Thread(() -> {
            try {
                destroy(context);
            } catch (IOException e) {
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private ThreadPoolExecutor getExecutor() {
        return new ThreadPoolExecutor(8, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),

                r -> new Thread(Thread.currentThread().getThreadGroup(), r, "web-bio-Server"));
    }

    @Override
    public void destroy(WebServerContext context) throws IOException {
        start = false;
        if (Objects.nonNull(executor))
            executor.shutdown();
        executor = null;
    }
}
