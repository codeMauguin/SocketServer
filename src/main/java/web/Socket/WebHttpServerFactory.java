package web.Socket;

import server.Server;
import web.server.WebServerContext;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;


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
        String finalServerName = "bio";
        if (this instanceof HttpNioServer) {
            finalServerName = "nio";
        }
        String finalServerName1 = finalServerName;
        executor = new ThreadPoolExecutor(8, 16, 2L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(8),
                r -> new Thread(currentThread().getThreadGroup(), r,
                        "web-%s-Server".formatted(finalServerName1)),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }


    @Override
    public void destroy(WebServerContext context) throws IOException {
        start = false;
        if (Objects.nonNull(executor))
            executor.shutdown();
        executor = null;
    }
}
