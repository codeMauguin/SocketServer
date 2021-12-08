package web.Socket.Handle;

import Logger.Logger;
import web.Socket.Reader;
import web.http.HttpRequest;
import web.http.HttpResponse;
import web.http.Imlp.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:22 下午 2021/12/5
 * @Modified By:
 */
public class WriteHandle implements EventHandle<SelectionKey> {
    private final ThreadPoolExecutor executor;
    private final Selector selector;

    public WriteHandle(ThreadPoolExecutor executor, Selector selector) {
        this.executor = executor;
        this.selector = selector;
    }

    @Override
    public void handle(SelectionKey key) {
        executor.submit(new Write(key, selector));
    }

    private static final class Write extends HttpHandle {
        private final SelectionKey key;
        private final Selector selector;
        private HttpServletResponse response;
        private ByteArrayOutputStream responseOutputStream;
        private ByteBuffer buffer;

        public Write(SelectionKey key, Selector selector) {
            super(null);
            this.key = key;
            this.selector = selector;
        }


        @Override
        public void start() {
            //整理response数据

            Logger.info("响应");
            response = (HttpServletResponse) key.attachment();
            init(null, response, null);
            StringBuilder builder = new StringBuilder();
            prepareResponse();
            initHttpState(builder, response);
            initHttpHeader(builder, response);
            initBlank(builder);
            buffer = prepareBuffer(builder);
        }

        @Override
        protected void init(HttpRequest request, HttpResponse response, Reader reader) {
            super.init(request, response, reader);
            this.responseOutputStream = (ByteArrayOutputStream) response.getOutputStream();
        }

        @Override
        public void destroy() {
            //想客户端推送数据
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                writeBuffer(buffer, channel);
                if (responseOutputStream.size() > 0) {
                    writeBuffer(ByteBuffer.wrap(responseOutputStream.toByteArray()), channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            registerAgain();
        }

        private void registerAgain() {
            SelectableChannel channel = key.channel();
            try {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
                selector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeBuffer(ByteBuffer buffer, SocketChannel channel) throws IOException {
            try {
                while (buffer.hasRemaining()) {

                    channel.write(buffer);

                }
            } catch (IOException e) {
                e.printStackTrace();
                key.channel().close();
                throw new IOException("Broken pipe");
            }
        }


        @Override
        protected ByteBuffer prepareBuffer(Object b) {
            StringBuilder builder = (StringBuilder) b;
            String var0 = builder.toString();
            byte[] bytes;
            try {
                bytes = var0.getBytes(response.getResponseUnicode());
            } catch (UnsupportedEncodingException e) {
                bytes = var0.getBytes(StandardCharsets.UTF_8);
            }
            return ByteBuffer.wrap(bytes);
        }
    }

}
