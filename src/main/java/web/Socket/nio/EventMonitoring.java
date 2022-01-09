package web.Socket.nio;

import com.whit.Logger.Logger;
import web.Socket.Handle.HttpHandle;
import web.Socket.WebHttpServerFactory;
import web.Socket.WebSockServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:26 上午 2021/12/5
 * @Modified By:
 */
public class EventMonitoring implements WebSockServer {
    private final Selector selector;
    private final ThreadPoolExecutor executor;
    private final WebHttpServerFactory factory;
    private volatile boolean start = true;

    public EventMonitoring(Selector selector, WebHttpServerFactory factory, ThreadPoolExecutor executor) {
        this.selector = selector;
        this.executor = executor;
        this.factory = factory;
    }


    @Override
    public void start() {
        Logger.info("开始监听");
        try {
            while (start) {
                factory.handle();
                int size = selector.select();
                if (size > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (!key.isValid()) continue;
                        try {
                            if (key.isAcceptable()) {
                                accept(key, selector);
                            } else if (key.isReadable()) {
                                cancel(key);
                                HttpHandle handle = factory.getHandle();
                                handle.release(key);
                                executor.execute(handle);
                                Logger.info("线程池活跃线程数:{0}", executor.getActiveCount());
                            } else if (key.isConnectable()) {
                                cancel(key);
                                System.out.println("key = " + key);
                            }
                        } catch (Exception e) {
                            key.cancel();
                            e.printStackTrace();
                            System.exit(0);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cancel(SelectionKey key) {
        key.cancel();
    }


    @Override
    public void destroy() {
        start = false;
    }

    private void accept(SelectionKey key, Selector selector) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            server.configureBlocking(false);
            SocketChannel channel = server.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ & ~SelectionKey.OP_WRITE, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        start();
    }
}
