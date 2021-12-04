package web.Socket;

import Logger.Logger;
import org.context.util.MessageUtil;
import web.http.Filter.FilterRecord;
import web.http.Header.Impl.HttpHeaderBuild;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.HttpRequest;
import web.http.HttpResponse;
import web.http.Imlp.HttpServletRequest;
import web.http.Imlp.HttpServletResponse;
import web.http.Libary.*;
import web.server.WebServerContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author 陈浩
 */
public final class SockAccept implements Runnable {
    private final static Pattern JSON_MATCH = Pattern.compile(".*json.*", Pattern.CASE_INSENSITIVE);
    private final static Pattern FORM_MATCH = Pattern.compile(".*x-www-form-urlencoded.*", Pattern.CASE_INSENSITIVE);
    private final static Map<String, String> optionHeaders = new LinkedHashMap<>();

    static {

        optionHeaders.put("Access-Control-Allow-Methods", "POST");
        optionHeaders.put("Access-Control-Allow-Headers", "content-type");
        optionHeaders.put("Access-Control-Max-Age", "1800");
        optionHeaders.put("Allow", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");
        optionHeaders.put("Content-Length", "0");
        optionHeaders.put("Connection", "keep-alive");
    }

    private final Socket accept;
    private final WebServerContext context;
    private OutputStream outputStream;
    private Reader reader;

    /**
     *
     */
    public SockAccept(Socket accept, WebServerContext context) {
        this.accept = accept;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            int timeout = 6000;
            InputStream inputStream = accept.getInputStream();
            outputStream = accept.getOutputStream();
            accept.setKeepAlive(true);
            reader = new Reader(new ReaderInputSteam(inputStream));
            MessageUtil messageUtil;
            while (accept.isConnected() && !accept.isClosed()) {
                HttpHeaderBuild h = new HttpHeaderBuilder();
                Logger.info(String.valueOf(accept.getPort()));
                accept.setSoTimeout(timeout);
                HttpInfo httpInfo;
                HttpHeaderInfo headerInfo = new HttpHeaderInfo();
                /*
                  读取信息
                 */
                String http = reader.readLine();
                Logger.info(String.format("http:%s", http));
                if ("".equals(http.trim())) {
                    /*
                       如果读取为空此次请求错误
                     */
                    Logger.info("此次请求错误");
                    continue;
                } else {
                    httpInfo = HttpInfo.getInstance(http);
                }
                Logger.info(String.format("method:%s", httpInfo.method()));
                Logger.info(String.format("path:%s", httpInfo.path()));
                /*
                  	读取请求头
                 */
                ReadRequestHeader(reader, headerInfo, h);
                HttpRequestPojo requestPojo = new HttpRequestPojo(httpInfo);
                HttpServletRequest request = new HttpServletRequest(inputStream, h, requestPojo);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HttpServletResponse response = new HttpServletResponse(byteArrayOutputStream, String.valueOf(timeout / 1000));
                if (!headerInfo.getOrigin().equals(""))
                    response.setOrigin(headerInfo.getOrigin());
                /*
                 * 处理过滤
                 */
                for (FilterRecord filter : context.getFilter(httpInfo.path())) {
                    filter.filter().doFilter(request, response);
                }

                if (headerInfo.getLength() > 0) {
                    messageUtil = readBody(headerInfo, httpInfo);
                } else {
                    messageUtil = new MessageUtil(httpInfo.params(), null, headerInfo.getType());
                }
                if (httpInfo.method().equals("OPTIONS")) {
                    optionHandle(httpInfo.version(), headerInfo.getOrigin(), String.valueOf(timeout / 1000));
                    return;
                }
                /*
                 * 处理请求控制器
                 */
                ControllerRecord controller = context.getController(httpInfo.path());
                if (Objects.isNull(controller)) {
                    response.setCode(HttpCode.HTTP_404);
                    error(response);
                } else if (controller.isServlet()) {
                    switch (httpInfo.method()) {
                        case "GET" -> controller.getServlet().doGet(request, response);
                        case "POST" -> controller.getServlet().doPost(request, response);
                        default -> {
                            response.setCode(HttpCode.HTTP_405);
                            error(response);
                        }
                    }
                } else {
                    ControllerMethod method = controller.getMethod(httpInfo.path());
                    if (
                            method.getMapper(httpInfo.method())
                    ) {
                        Parameter[] parameters = method.getParameters();
                        Object[] args;
                        String[] paramNames = method.getParamNames();
                        int index =
                                parameters.length - Math.toIntExact(Arrays.stream(parameters).filter(var -> HttpRequest.class.isAssignableFrom(var.getType()) || HttpResponse.class.isAssignableFrom(var.getType())).count());
                        args = resolveMethodArgs(parameters, paramNames, messageUtil, index, request, response);
                        method.getMethod().setAccessible(true);
                        Object invoke = method.getMethod().invoke(controller.getInstance(), args);
                        Class<?> returnType = method.getMethod().getReturnType();
                        if (!returnType.equals(Void.class)) {
                            if (MessageUtil.isPrimitive(returnType)) {
                                byteArrayOutputStream.write(invoke.toString().getBytes(response.getResponseUnicode()));
                            } else {
                                byteArrayOutputStream.write(MyJSON.JSON.ObjectToString(invoke).getBytes(response.getResponseUnicode()));
                            }
                        }
                    } else {
                        response.setCode(HttpCode.HTTP_405);
                        error(response);
                    }
                }
                Map<String, String> headers = response.getHeaders();
                headers.put("Access-control-allow-credentials", "true");
                headers.put("Access-Control-Allow-Origin", response.getOrigin());
                response.setLength(byteArrayOutputStream.size());
                writeHttpInfo(httpInfo.version(), response.getCode(), response.getResponseUnicode());
                printHead(headers, response.getResponseUnicode());
                /*
                 * 输出换行
                 */
                outputStream.write(NetworkLibrary.CRLF.getContent().getBytes());
                if (byteArrayOutputStream.size() > 0)
                    byteArrayOutputStream.writeTo(outputStream);
                outputStream.flush();
            }
        } catch (IOException e) {
            try {
                accept.close();
            } catch (IOException ignored) {
            }
            Logger.info(accept.getPort() + "已经断开链接");
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private Object[] resolveMethodArgs(Parameter[] parameters, String[] paramNames, MessageUtil messageUtil, int index,
                                       HttpServletRequest request, HttpServletResponse response) {
        Object[] args = new Object[parameters.length];
        for (int i = 0, paramNamesLength = paramNames.length; i < paramNamesLength; i++) {
            String paramName = paramNames[i];
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if (type == HttpRequest.class || type == HttpServletRequest.class) {
                args[i] = request;
            } else if (type == HttpResponse.class || type == HttpServletResponse.class) {
                args[i] = response;
            } else {
                args[i] = messageUtil.resolve(paramName, type, index);
            }
        }
        return args;
    }

    private void writeHttpInfo(String version, HttpCode code, String unicode) throws IOException {
        outputStream.write(String.format(NetworkLibrary.HTTP_HEADER.getContent(), version,
                code.getCode(),
                code.getMsg()).getBytes(unicode));
    }

    private MessageUtil readBody(HttpHeaderInfo headerInfo, HttpInfo info) throws IOException {
        byte[] readByteArray = reader.readByteArray(headerInfo.getLength());
        String body = URLDecoder.decode(new String(readByteArray, 0, readByteArray.length), headerInfo.getCharset());
        return new MessageUtil(info.params(), body, headerInfo.getType());
    }

    private void error(HttpServletResponse response) {
        response.setCharset("UTF-8");
        response.setContentType("text/html;charset=utf-8;");

        //"{\"code\":\"" + response.getCode().getCode() + "\",\"msg\":\"" + response.getCode().getMsg() + "\"}"
        String html = """
                <img width="100%" height="100%" src="http://www.177347.com/d/file/2018/08-12/b99a3abea3c2694213c44e0508018e5d.jpg" />
                 """;
        response.getPrintSteam().println(html);
    }

    private void ReadRequestHeader(Reader reader, HttpHeaderInfo headerInfo, HttpHeaderBuild h) throws IOException {
        String line;
        while (!(line = reader.readLine().trim()).equals("")) {
            int split = line.indexOf(":");
            String key = line.substring(0, split).trim();
            String value = line.substring(split + 1).trim();
            switch (key) {
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
            h.setHeader(key, value);
        }


    }

    private void optionHandle(String version, String origin, String timeout) throws IOException {
        outputStream.write("HTTP/%s 200\r\n".formatted(version).getBytes(StandardCharsets.UTF_8));
        outputStream.write("Vary: Origin\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.write("Vary: Access-Control-Request-Method\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.write("Vary: Access-Control-Request-Headers\r\n".getBytes(StandardCharsets.UTF_8));
        optionHeaders.put("Access-Control-Allow-Origin", origin);
        optionHeaders.put("Date", LocalDateTime.now().toString());
        optionHeaders.put("Keep-Alive", "timeout=" + timeout);
        printHead(optionHeaders, "UTF-8");
        outputStream.write(NetworkLibrary.CRLF.getContent().getBytes());
    }

    private void printHead(Map<String, String> headers, String unicode) throws IOException {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            outputStream.write(String.format(NetworkLibrary.HTTP_HEADER_MODEL.getContent(), k.trim(), v.trim()).getBytes(unicode));
        }
    }


}
