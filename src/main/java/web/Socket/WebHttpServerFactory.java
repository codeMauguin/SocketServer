package web.Socket;

import server.Server;
import web.server.WebServerContext;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class WebHttpServerFactory implements Server<WebServerContext> {
    protected ThreadPoolExecutor executor;

    protected volatile boolean start;


    @Override
    public void start(WebServerContext context) throws Throwable {
        initExecutor();

        initStart(context);
    }

    private void initStart(WebServerContext context) {
        start = true;
        Thread hook = new Thread(() -> {
            try {
                destroy(context);
            } catch (IOException ignored) {
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private void initExecutor() {
        executor = new ThreadPoolExecutor(8, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),

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
