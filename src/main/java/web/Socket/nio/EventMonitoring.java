package web.Socket.nio;

import web.Socket.Handle.EventHandle;
import web.Socket.Handle.NioHttpHandle;
import web.Socket.WebSockServer;
import web.server.WebServerContext;

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
    private final WebServerContext context;
    private final ThreadPoolExecutor executor;
    private volatile boolean start = true;

    private EventHandle<SelectionKey> reader;
    private EventHandle<SelectionKey> write;

    public EventMonitoring(Selector selector, WebServerContext context, ThreadPoolExecutor executor) {
        this.selector = selector;
        this.context = context;
        this.executor = executor;
    }

    @Override
    public void start() {
        try {
            while (start) {
                int size = selector.select(10);
                if (size > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        try {
                            iterator.remove();
                            if (key.isAcceptable()) {
                                accept(key, selector);
                            } else if (key.isReadable() & key.isWritable()) {
                                key.cancel();
                                executor.submit(new NioHttpHandle(context, key));
                            } else if (key.isConnectable()) {
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

    @Override
    public void destroy() {
        start = false;
    }

    private void accept(SelectionKey key, Selector selector) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel channel = server.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
