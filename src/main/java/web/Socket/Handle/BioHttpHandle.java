package web.Socket.Handle;

import com.whit.Logger.Logger;
import org.context.util.MessageUtil;
import web.Socket.InputStream.BioReaderInputStream;
import web.Socket.Reader;
import web.http.Header.HttpHeader;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.Imlp.HttpServletRequest;
import web.http.Imlp.HttpServletResponse;
import web.http.Libary.HttpHeaderInfo;
import web.http.Libary.HttpInfo;
import web.http.Libary.HttpRequestRecord;
import web.server.WebServerContext;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static web.http.Controller.HttpOptionRequest.handle;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 1:16 下午 2021/12/6
 * @Modified By:
 */
public class BioHttpHandle extends HttpHandle {
    private final Socket accept;

    private InputStream inputStream;
    private OutputStream outputStream;

    private HttpHeaderInfo headerInfo;
    private HttpInfo info;


    public BioHttpHandle(Socket accept, WebServerContext context) {
        super(context);
        this.accept = accept;

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
            inputStream = accept.getInputStream();
            outputStream = accept.getOutputStream();
            while (accept.isConnected() && !accept.isClosed()) {
                reader = new Reader(new BioReaderInputStream(inputStream));
                try {
                    info = initHttpInfo(reader);
                } catch (IllegalArgumentException ignore) {
                    continue;
                }
                headerInfo = new HttpHeaderInfo(info);
                start();
                destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            Logger.info("RemoteAddress:{0}", accept.getRemoteSocketAddress());

            Logger.info("path:{0}", info.path());
            Logger.info("method:{0}", info.method());
            headerInfo.setTimeout(context.getTimeout());
            HttpRequestRecord pojo = new HttpRequestRecord(info);
            HttpHeader httpHeader = new HttpHeaderBuilder(HttpHandle.headerReader(reader, getHeaderHandle(headerInfo)));
            request = new HttpServletRequest(inputStream, httpHeader, pojo);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response = new HttpServletResponse(outputStream, headerInfo);
            init(request, response, reader);
            handle(context, request, (HttpServletResponse) response);
            doFilter(request, response);
            MessageUtil util = checkBody(headerInfo);
            if (!request.getMethod().equals("OPTIONS")) {
                try {
                    doInvoke(util);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
