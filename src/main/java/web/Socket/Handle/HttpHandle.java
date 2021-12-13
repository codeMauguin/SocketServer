package web.Socket.Handle;

import com.whit.Logger.Logger;
import org.context.util.MessageUtil;
import web.Socket.Reader;
import web.Socket.WebSockServer;
import web.http.Filter.FilterRecord;
import web.http.HttpRequest;
import web.http.HttpResponse;
import web.http.Imlp.HttpServletResponse;
import web.http.Libary.*;
import web.server.WebServerContext;
import web.util.Assert;
import web.util.TypeConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:33 上午 2021/12/6
 * @Modified By:
 */
public abstract class HttpHandle implements WebSockServer {
    private final static Pattern JSON_MATCH = Pattern.compile(".*json.*", Pattern.CASE_INSENSITIVE);
    private final static Pattern FORM_MATCH = Pattern.compile(".*x-www-form-urlencoded.*", Pattern.CASE_INSENSITIVE);
    protected HttpRequest request;
    protected HttpResponse response;
    protected Reader reader;
    protected WebServerContext context;

    public HttpHandle(WebServerContext context) {
        this.context = context;
    }

    public static Map<String, String> headerReader(Reader reader, BiConsumer<String, String> action) throws IOException {
        Map<String, String> result = new IdentityHashMap<>();
        String line;
        while (!(line = reader.readLine().trim()).equals("")) {
            int split = line.indexOf(":");
            String key = line.substring(0, split).trim();
            String value = line.substring(split + 1).trim();
            result.put(key, value);
            action.accept(key, value);
        }
        return result;
    }

    public static void headerHandle(HttpHeaderInfo headerInfo, String p, String value) {
        switch (p) {
            case "Content-Type" -> {
                Logger.info("Content-Type:{0}", value);
                int start = value.indexOf("=") + 1;
                int end;
                if (start > 0) {
                    if (value.lastIndexOf(";") > start)
                        end = value.length() - 1;
                    else
                        end = value.length();
                    headerInfo.setCharset(value.substring(start, end));
                    Logger.info("charset:{0}", headerInfo.getCharset());
                }
                if (JSON_MATCH.matcher(value).matches()) {
                    headerInfo.setType(
                            "JSON"
                    );
                } else if (FORM_MATCH.matcher(value).matches()) {
                    headerInfo.setType(
                            "FORM"
                    );
                }
            }
            case "Content-Length" -> {
                Logger.info("Content-Length:{0}", value);
                headerInfo.setLength(Integer.parseInt(value));
            }
            case "Origin" -> {
                headerInfo.setOrigin(value);
                Logger.info("origin:{0}", value);
            }
        }
    }

    protected static void initHttpState(StringBuilder builder, HttpServletResponse response) {
        if (Objects.equals(response.getMethod(), "OPTIONS")) {
            builder.append("HTTP/%s %d\r\n".formatted(response.getVersion(), response.getCode().getCode()));
        } else {
            builder.append(NetworkLibrary.HTTP_HEADER.getContent().formatted(response.getVersion(),
                    response.getCode().getCode(), response.getCode().getMsg()));
        }
    }

    protected static void initBlank(StringBuilder builder) {
        builder.append("\r\n");
    }

    protected static void initHttpHeader(StringBuilder builder, HttpServletResponse response) {
        for (Map.Entry<String, List<String>> next : response.getHeaders().entrySet()) {
            String var1 = next.getKey();
            List<String> var2 = next.getValue();
            for (String var3 : var2) {
                builder.append(String.format(NetworkLibrary.HTTP_HEADER_MODEL.getContent(), var1.trim(),
                        var3.trim()));
            }
        }

    }


    protected void doInvoke(MessageUtil util) throws Exception {
        HttpServletResponse resp = (HttpServletResponse) response;
        ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) response.getOutputStream();
        ControllerRecord controller = context.getController(request.getPath());
        if (Objects.isNull(controller)) {
            resp.setCode(HttpCode.HTTP_404);
//            error(response);
        } else if (controller.isServlet()) {
            switch (request.getMethod()) {
                case "GET" -> controller.getServlet().doGet(request, response);
                case "POST" -> controller.getServlet().doPost(request, response);
                default -> {
                }
            }
        } else {
            ControllerMethod method = controller.getMethod(request.getPath());
            if (
                    method.getMapper(request.getMethod())
            ) {
                Parameter[] parameters = method.getParameters();
                Object[]
                        args = util.resolve(parameters, request, response);
                method.getMethod().setAccessible(true);
                Object invoke = method.getMethod().invoke(controller.getInstance(), args);
                Class<?> returnType = method.getMethod().getReturnType();
                if (!returnType.equals(Void.class)) {
                    if (TypeConverter.isPrimitive(returnType)) {
                        byteArrayOutputStream.write(invoke.toString().getBytes(resp.getResponseUnicode()));
                    } else {
                        byteArrayOutputStream.write(MyJSON.JSON.ObjectToString(invoke).getBytes(resp.getResponseUnicode()));
                    }
                }
            } else {
                resp.setCode(HttpCode.HTTP_405);
//                error(response);
            }
        }
        response.setContentType("application/json;charset=utf-8;");
    }

    protected void init(HttpRequest request, HttpResponse response, Reader reader) {
        this.request = request;
        this.response = response;
        this.reader = reader;
    }

    protected void doFilter(HttpRequest request, HttpResponse response) {
        for (FilterRecord filter : context.getFilter(request.getPath())) {
            filter.filter().doFilter(request, response);
        }
    }

    protected MessageUtil checkBody(HttpHeaderInfo headerInfo) {
        MessageUtil util;
        if (headerInfo.getLength() > 0) {
            byte[] bytes = new byte[0];
            try {
                bytes = reader.readByteArray(headerInfo.getLength());
            } catch (IOException ignored) {
            }
            util = new MessageUtil(headerInfo.getInfo().params(), bytes,
                    headerInfo.getType());
        } else {
            util = new MessageUtil(headerInfo.getInfo().params(), null, headerInfo.getType());
        }
        return util;
    }

    protected abstract Object prepareBuffer(Object buffer);

    protected void prepareResponse() {
        ByteArrayOutputStream responseOutputStream = ((ByteArrayOutputStream) response.getOutputStream());
        ((HttpServletResponse) response).setLength(responseOutputStream.size());
    }


    protected HttpInfo initHttpInfo(Reader reader) throws IOException {
        String info = reader.readLine();
        Assert.notNull(info, "请求头信息不允许为空");
        return HttpInfo.getInstance(info);
    }

    protected BiConsumer<String, String> getHeaderHandle(HttpHeaderInfo headerInfo) {
        return (p, value) -> headerHandle(headerInfo, p, value);
    }
}
