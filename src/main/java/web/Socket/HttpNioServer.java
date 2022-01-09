package web.Socket;

import com.whit.Logger.Logger;
import web.Socket.nio.EventMonitoring;
import web.server.WebServerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 9:51 上午 2021/12/5
 * @Modified By:
 */
public class HttpNioServer extends WebHttpServerFactory {
    private Selector selector;

    private EventMonitoring eventMonitoring;

    @Override
    public void start(WebServerContext context) throws Throwable {
        super.start(context);
        initChannel(context);
        initListener(context);
        executor.execute(eventMonitoring);
        Logger.info("The program is running and starting up:{0}ms", System.currentTimeMillis() - context.getStart());
    }

    @Override
    public void destroy(WebServerContext context) throws IOException {
        eventMonitoring.destroy();
        super.destroy(context);
        Logger.info("Service stops on port ".concat(String.valueOf(context.getPort())));
    }

    private void initListener(WebServerContext context) {
        eventMonitoring = new EventMonitoring(selector, this, executor);
    }

    private void initChannel(WebServerContext context) throws Throwable {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(context.getIp(), context.getPort()));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        Logger.info("http Server start in port " + context.getPort());
    }
}
