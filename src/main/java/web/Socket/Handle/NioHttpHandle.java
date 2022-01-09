package web.Socket.Handle;

import com.whit.Logger.Logger;
import web.Socket.InputStream.NioReaderInputStream;
import web.Socket.InputStream.ReaderInputStream;
import web.Socket.Reader;
import web.http.Header.HttpHeader;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.Libary.HttpHeaderInfo;
import web.http.Libary.HttpInfo;
import web.http.Libary.HttpRequestRecord;
import web.server.WebServerContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 6:41 下午 2021/12/10
 * @Modified By: 陈浩
 */
public class NioHttpHandle extends HttpHandle {
    private SelectionKey key;
    private ReaderInputStream inputStream;
    private HttpInfo info;
    private SocketChannel client;
    private HttpServletResponse response;
    private ByteArrayOutputStream responseOutputStream;

    public NioHttpHandle(WebServerContext context) {
        super(context);
    }


    public void release(Object key) {
        this.key = (SelectionKey) key;
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

    @Override
    public void run() {
        super.setStatus(HttpHandle.CHOKE);
        //TODO 客户端已经断开连接，服务端没有感知 判断第一个是否读取为空，为空则关闭这个可以并且取消监听
        client = (SocketChannel) key.channel();
        inputStream = new NioReaderInputStream(client);
        reader = new Reader(inputStream);
        try {
            Logger.info("RemoteAddress:{0}", client.getRemoteAddress());
            info = initHttpInfo(reader);
        } catch (IllegalArgumentException | IOException e) {
            try {
                key.cancel();
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        super.run();
    }

    @Override
    public void start() {
        try {
            headerInfo = new HttpHeaderInfo(info);
            headerInfo.setTimeout(String.valueOf(context.getTimeout() / 1000));
            HttpRequestRecord pojo = new HttpRequestRecord(info);
            HttpHeader httpHeader = new HttpHeaderBuilder(headerReader(reader, getHeaderHandle(headerInfo)));
            request = new HttpServletRequest(inputStream, httpHeader, pojo);
            this.responseOutputStream = new ByteArrayOutputStream();
            response = new HttpServletResponse(this.responseOutputStream, headerInfo);
            init(request, response, reader);
            Logger.info("path:{0}", info.path());
            Logger.info("method:{0}", info.method());
            //处理消息
//            进入过滤器
            doFilter(request, response);
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
        }
    }

    @Override
    public void destroy() {

//         check if you need to read the body
        //Look for a real image parser to exclude OPTIONS requests or non-allowed domain requests
        StringBuilder builder = new StringBuilder();
        prepareResponse();
        initHttpState(builder, response);
        initHttpHeader(builder, response);
        initBlank(builder);
        ByteBuffer buffer = prepareBuffer(builder);
        try {
            writeBuffer(buffer, client);
            if (responseOutputStream.size() > 0) {
                writeBuffer(ByteBuffer.wrap(responseOutputStream.toByteArray()), client);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (headerInfo.isConnection()) registerAgain();
        else {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() throws IOException {
        client.close();
    }

    private void registerAgain() {
        try {
            if (!client.isRegistered()) client.register(key.selector(), SelectionKey.OP_READ & ~SelectionKey.OP_WRITE);
        } catch (CancelledKeyException exception) {
            exception.printStackTrace();
        } catch (Exception ignored) {
        }
        key.selector().wakeup();//Wake up the selector, otherwise it will block until a new request will listen to the socket
    }
}
