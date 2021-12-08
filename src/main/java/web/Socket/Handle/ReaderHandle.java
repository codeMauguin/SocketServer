package web.Socket.Handle;

import Logger.Logger;
import org.context.util.MessageUtil;
import web.Socket.InputStream.NioReaderInputStream;
import web.Socket.InputStream.ReaderInputStream;
import web.Socket.Reader;
import web.http.Controller.HttpOptionRequest;
import web.http.Header.HttpHeader;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.Imlp.HttpServletRequest;
import web.http.Imlp.HttpServletResponse;
import web.http.Libary.HttpHeaderInfo;
import web.http.Libary.HttpInfo;
import web.http.Libary.HttpRequestRecord;
import web.server.WebServerContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:22 下午 2021/12/5
 * @Modified By:
 */
public class ReaderHandle implements EventHandle<SelectionKey> {

    private final ThreadPoolExecutor executor;
    private final WebServerContext context;

    private final Selector selector;

    public ReaderHandle(ThreadPoolExecutor executor, WebServerContext context, Selector selector) {
        this.executor = executor;
        this.context = context;
        this.selector = selector;
    }

    @Override
    public void handle(SelectionKey key) {
        executor.submit(new Readers(context, key, selector));
    }

    private static final class Readers extends HttpHandle {
        private final SelectionKey key;
        private final Selector selector;
        private HttpHeaderInfo headerInfo;
        private ReaderInputStream inputStream;

        private HttpInfo info;
        private SocketChannel client;

        public Readers(WebServerContext context, SelectionKey key, Selector selector) {
            super(context);
            this.key = key;
            this.selector = selector;
        }

        @Override
        public void run() {
            client = (SocketChannel) key.channel();
            inputStream = new NioReaderInputStream(client);
            reader = new Reader(inputStream);
            try {
                info = initHttpInfo(reader);
            } catch (IllegalArgumentException | IOException e) {
                try {
                    key.channel().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
            super.run();
        }

        @Override
        public void start() {
            //读取io数据
            try {
                headerInfo = new HttpHeaderInfo(info);
                headerInfo.setTimeout(context.getTimeout());
                HttpRequestRecord pojo = new HttpRequestRecord(info);
                HttpHeader httpHeader = new HttpHeaderBuilder(headerReader(reader, getHeaderHandle(headerInfo)));
                request = new HttpServletRequest(inputStream, httpHeader, pojo);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                response = new HttpServletResponse(outputStream, headerInfo);
                init(request, response, reader);
                Logger.info("RemoteAddress:{0}", client.getRemoteAddress());
                Logger.info("path:{0}", info.path());
                Logger.info("method:{0}", info.method());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void destroy() {
            //处理消息
//            对跨域处理
            HttpOptionRequest.handle(context, request, (HttpServletResponse) response);
//            进入过滤器
            doFilter(request, response);
//            检查是否需要读取body
            MessageUtil util = checkBody(headerInfo);
            //寻找实图解析器
            if (!request.getMethod().equals("OPTIONS")) {
                try {
                    doInvoke(util);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            key.attach(response);
            registerAgain();
        }

        private void registerAgain() {
            SelectableChannel channel = key.channel();
            try {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_WRITE, response);
                selector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        @Deprecated
        protected Object prepareBuffer(Object buffer) {
            return null;
        }
    }
}
