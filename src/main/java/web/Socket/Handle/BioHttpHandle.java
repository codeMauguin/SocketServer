package web.Socket.Handle;

import com.whit.Logger.Logger;
import web.Socket.Reader;
import web.Socket.io.InputStream.BioReaderInputStream;
import web.Socket.io.InputStream.ReaderInputStream;
import web.http.Header.HttpHeader;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.Libary.HttpHeaderInfo;
import web.http.Libary.HttpInfo;
import web.http.Libary.HttpRequestRecord;
import web.server.WebServerContext;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 1:16 下午 2021/12/6
 * @Modified By:
 */
public class BioHttpHandle extends HttpHandle {
    private Socket accept;
    private LocalDateTime start;
    private InputStream inputStream;

    private ReaderInputStream readerInputStream;
    private OutputStream outputStream;
    private HttpInfo info;


    public BioHttpHandle(WebServerContext context) {
        super(context);
    }

    @Override
    public void release(Object key) {
        if (key instanceof Socket socket) {
            this.accept = socket;
        } else throw new RuntimeException("Error Token" + key);
    }

    @Override
    protected byte[] prepareBuffer(Object buffer) {
        StringBuilder builder = (StringBuilder) buffer;
        String var0 = builder.toString();
        byte[] bytes;
        try {
            bytes = var0.getBytes(((HttpServletResponse) response).getResponseUnicode());
        } catch (UnsupportedEncodingException e) {
            bytes = var0.getBytes(StandardCharsets.UTF_8);
        }
        return bytes;
    }

    @Override
    public void run() {
        try {
            start = LocalDateTime.now();
            inputStream = accept.getInputStream();
            outputStream = accept.getOutputStream();
            readerInputStream = new BioReaderInputStream(inputStream);
            reader = new Reader(readerInputStream);
            while (!accept.isClosed() && (headerInfo == null || headerInfo.isConnection()) && isNotTimeOut()) {
                try {
                    info = initHttpInfo(reader);
                } catch (IllegalArgumentException ignore) {
                    if (inputStream.available() == 0) accept.close();
                    continue;
                }
                headerInfo = new HttpHeaderInfo(info);
                super.run();
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        try {
            outputStream.close();
            inputStream.close();
            accept.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNotTimeOut() {
        return start.plusSeconds(context.getTimeout() / 1000).isAfter(LocalDateTime.now());
    }

    @Override
    public void start() {
        try {
            Logger.info("RemoteAddress:{0}", accept.getRemoteSocketAddress());
            Logger.info("path:{0}", info.path());
            Logger.info("method:{0}", info.method());
            headerInfo.setTimeout(String.valueOf(context.getTimeout() / 1000));
            HttpRequestRecord pojo = new HttpRequestRecord(info);
            HttpHeader httpHeader = new HttpHeaderBuilder(HttpHandle.headerReader(reader, getHeaderHandle(headerInfo)));
            request = new HttpServletRequest(readerInputStream, httpHeader, pojo);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response = new HttpServletResponse(outputStream, headerInfo);
            init(request, response, reader);
            doFilter(request, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        StringBuilder builder = new StringBuilder();
        prepareResponse();
        initHttpState(builder, (HttpServletResponse) response);
        initHttpHeader(builder, (HttpServletResponse) response);
        initBlank(builder);
        byte[] bytes = prepareBuffer(builder);
        try {
            outputStream.write(bytes);
            ByteArrayOutputStream stream = (ByteArrayOutputStream) response.getOutputStream();
            if (stream.size() > 0) {
                stream.writeTo(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
