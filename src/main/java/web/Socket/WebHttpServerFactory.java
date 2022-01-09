package web.Socket;

import server.Server;
import web.Socket.Handle.HttpHandle;
import web.server.WebServerContext;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;


public abstract class WebHttpServerFactory implements Server<WebServerContext> {
    private final LinkedList<HttpHandle> HANDLE_POOL = new LinkedList<>();
    private final LinkedList<HttpHandle> READY_TO_HANDLE_POOL = new LinkedList<>();
    protected ThreadPoolExecutor executor;
    protected volatile boolean start;
    private WebServerContext context;

    private HttpHandle createHandle(WebServerContext context) throws Exception {
        return context.getServerType().getDeclaredConstructor(WebServerContext.class).newInstance(context);
    }

    @Override
    public void start(WebServerContext context) throws Throwable {
        this.context = context;
        initExecutor();

        initStart();

        initHandlePool();
    }

    private void initHandlePool() throws Exception {
        for (int i = 0; i < 8; i++) {
            HANDLE_POOL.offer(createHandle(context));
        }
    }

    public HttpHandle getHandle() throws Exception {
        HttpHandle httpHandle = HANDLE_POOL.pollFirst();
        return Objects.nonNull(httpHandle) ? httpHandle : createHandle(context);
    }

    @SuppressWarnings("all")
    public void handle() {
        READY_TO_HANDLE_POOL.removeIf(httpHandle -> httpHandle.isBE_READY() && (HANDLE_POOL.offer(httpHandle) || true));
    }

    private void initStart() {
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
        executor = new ThreadPoolExecutor(8, 16, 2L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(8), r -> new Thread(currentThread().getThreadGroup(), r, "web-%s-Server".formatted(finalServerName1)), new ThreadPoolExecutor.CallerRunsPolicy());
    }


    @Override
    public void destroy(WebServerContext context) throws IOException {
        start = false;
        if (Objects.nonNull(executor)) executor.shutdown();
        executor = null;
    }
}
