package web.Socket;

import Logger.Logger;
import com.alibaba.fastjson.JSON;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;
import server.Server;
import web.http.Filter.Filter;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author 陈浩
 */
public record SockAccept(Socket accept, WebServerContext context) implements Runnable {
    private final static Pattern JSON_MATCH = Pattern.compile(".*json.*", Pattern.CASE_INSENSITIVE);
    private final static Pattern FORM_MATCH = Pattern.compile(".*x-www-form-urlencoded.*", Pattern.CASE_INSENSITIVE);

    @Override
    public void run() {
        try {
            int timeout = 6000;
            InputStream inputStream = accept.getInputStream();
            OutputStream outputStream = accept.getOutputStream();
            accept.setKeepAlive(true);
            Reader reader = new Reader(inputStream);
            while (accept.isConnected() && !accept.isClosed()) {
                HttpHeaderBuild h = new HttpHeaderBuilder();
                Logger.info(String.valueOf(accept.getPort()));
                accept.setSoTimeout(timeout);
                HttpInfo httpInfo;
                HttpHeaderInfo headerInfo = new HttpHeaderInfo();
                /*
                  读取信息
                 */
                reader.start(-1);
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
                for (Filter filter : context.getFilter(httpInfo.path())) {
                    filter.doFilter(request, response);
                }

                if (headerInfo.getLength() > 0) {
                    reader.destroy(headerInfo.getLength());
                    String body = URLDecoder.decode(new String(reader.body, 0, reader.body.length), headerInfo.getCharset());
                    requestPojo.setBody(body);
                    switch (headerInfo.getType()) {
                        case "MyJSON" -> {
                            Map<String, Object> map = JSON.parseObject(body);
                            requestPojo.setParams(map);
                            Logger.info("body:{0}", body);
                        }
                        case "FORM" -> {
                            MultiMap<String> map = new MultiMap<>();
                            UrlEncoded.decodeTo(body, map, headerInfo.getCharset());
                            requestPojo.setParams(map);
                            Logger.info("body:{0}", map.get("id"));
                        }
                        default -> {
                            //将数据读取为byte数组
                        }
                    }
                }
                if (httpInfo.method().equals("OPTIONS")) {
                    optionHandle(outputStream, httpInfo.version(), headerInfo.getOrigin(), String.valueOf(timeout / 1000));
                    return;
                }
                /*
                 * 处理请求控制器
                 */

                ControllerRecord controller = context.getController(httpInfo.path());
                if (Objects.isNull(controller)) {
                    error(response);
                } else if (controller.isServlet()) {
                    try {
                        switch (httpInfo.method()) {
                            case "GET" -> controller.getServlet().doGet(request, response);
                            case "POST" -> controller.getServlet().doPost(request, response);
                        }
                    } catch (NullPointerException ignore) {
                        error(response);
                    }
                } else {
                    ControllerMethod method = controller.getMethod(httpInfo.path());
                    Parameter[] parameters = method.getParameters();
                    Object[] params = new Object[parameters.length];
                    String[] paramNames = method.getParamNames();
                    for (int i = 0, paramNamesLength = paramNames.length; i < paramNamesLength; i++) {
                        String paramName = paramNames[i];
                        Parameter parameter = parameters[i];
                        Class<?> type = parameter.getType();
                        if (type == HttpRequest.class || type == HttpServletRequest.class) {
                            params[i] = request;
                        } else if (type == HttpResponse.class || type == HttpServletResponse.class) {
                            params[i] = response;
                        } else if (String.class.equals(type) || type.isPrimitive() || char.class.equals(type) || int.class.equals(type) || boolean.class.equals(type) || long.class.equals(type) || float.class.equals(type) || byte.class.equals(type)) {
                            Object param = request.getParam(paramName);
                            if (type == int.class) {
                                params[i] = Integer.parseInt((String) param);
                            } else if (type == boolean.class) {
                                params[i] = Boolean.getBoolean((String) param);
                            } else
                                params[i] = type.cast(param);
                        } else {
                            params[i] = request.getParam(type);
                        }
                    }
                    method.getMethod().setAccessible(true);
                    Object invoke = method.getMethod().invoke(controller.getInstance(), params);
                    Class<?> returnType = method.getMethod().getReturnType();
                    if (returnType.equals(Void.class)) {
                        //TODO 跳过没有返回值
                    } else if (String.class.equals(returnType) || returnType.isPrimitive() || char.class.equals(returnType) || int.class.equals(returnType) || boolean.class.equals(returnType) || long.class.equals(returnType) || float.class.equals(returnType) || byte.class.equals(returnType)) {
                        byteArrayOutputStream.write(invoke.toString().getBytes(response.getResponseUnicode()));
                    } else {
                        byteArrayOutputStream.write(MyJSON.JSON.ObjectToString(invoke).getBytes(response.getResponseUnicode()));
                    }
                }
                outputStream.write(String.format(NetworkLibrary.HTTP_HEADER.getContent(), httpInfo.version(),
                        HttpCode.HTTP_200.getCode(),
                        HttpCode.HTTP_200.getMsg()).getBytes(response.getResponseUnicode()));
                outputStream.write("Access-control-allow-credentials:true\r\nAccess-Control-Allow-Origin:%s%s".formatted(response.getOrigin(), "\r\n").getBytes(response
                        .getResponseUnicode()));
                Map<String, String> headers = response.getHeaders();
                response.setLength(byteArrayOutputStream.size());
                printHead(headers, response.getResponseUnicode(), outputStream);
                /*
                 * 输出换行
                 */
                outputStream.write(NetworkLibrary.CRLF.getContent().getBytes());
                if (byteArrayOutputStream.size() > 0)
                    byteArrayOutputStream.writeTo(outputStream);
                outputStream.flush();
                reader.reload();
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

    private void error(HttpServletResponse response) {
        response.setCharset("UTF-8");
        response.getPrintSteam().println("{\"code\":\"404\",\"msg\":\"页面找不到\"}");
    }

    private void ReadRequestHeader(Reader reader, HttpHeaderInfo headerInfo, HttpHeaderBuild h) {
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
                                "MyJSON"
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

    private void optionHandle(OutputStream outputStream, String version, String origin, String timeout) throws IOException {
        outputStream.write("HTTP/%s 200\r\n".formatted(version).getBytes(StandardCharsets.UTF_8));
        outputStream.write("Vary: Origin\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.write("Vary: Access-Control-Request-Method\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.write("Vary: Access-Control-Request-Headers\r\n".getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", origin);
        headers.put("Access-Control-Allow-Methods", "POST");
        headers.put("Access-Control-Allow-Headers", "content-type");
        headers.put("Access-Control-Max-Age", "1800");
        headers.put("Allow", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");
        headers.put("Content-Length", "0");
        headers.put("Date", LocalDateTime.now().toString());
        headers.put("Keep-Alive", "timeout=" + timeout);
        headers.put("Connection", "keep-alive");
        printHead(headers, "UTF-8", outputStream);
        outputStream.write(NetworkLibrary.CRLF.getContent().getBytes());
    }

    private void printHead(Map<String, String> headers, String unicode, OutputStream outputStream) throws IOException {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            outputStream.write(String.format(NetworkLibrary.HTTP_HEADER_MODEL.getContent(), k.trim(), v.trim()).getBytes(unicode));
        }
    }

    private static class Reader implements Server<Integer> {
        private final InputStream inputStream;
        private int index = 0;
        private int readIndex = 0;
        private int size = 4096;
        private byte[] buffer = new byte[size];
        private byte[] body;

        public Reader(InputStream inputStream) {
            this.inputStream = inputStream;
        }


        @Override
        public void start(Integer k) throws IOException {
            //这个会阻塞
//            while (index < size) {
//                body = inputStream.read();
//                if (body == -1) {
//                    break;
//                }
//                buffer[index++] = (byte) body;
//                if (index >= size) {
//                    expansion();
//                }
//            }
            while (true) {
                int read = inputStream.read(buffer, index, size - index);
                if (read != -1) {
                    index += read;
                } else {
                    break;
                }
                if (index >= size) {
                    expansion();
                } else {
                    break;
                }
            }
        }

        private char byteToChar(byte b) {
            return (char) (b & 0xff);
        }

        public String readLine() {
            StringBuilder builder = new StringBuilder();
            int state = 0;
            while (readIndex < index) {
                byte datum = buffer[readIndex++];
                char str = byteToChar(datum);
                if ((state == 0 && str == '\r') || (state == 1 && str == '\n')) {
                    state++;
                } else {
                    if (state > 0)
                        break;
                    builder.append(str);
                }
                if (state == 2) {
                    break;
                }
            }
            return builder.toString();
        }

        /**
         * 扩容缓冲区
         */
        private void expansion() {
            this.size *= 2;
            this.buffer = Arrays.copyOf(this.buffer, this.size);
        }

        private void reload() throws IOException {
            index = 0;
            readIndex = 0;
            size = 4096;
            buffer = new byte[size];
            body = new byte[0];
        }

        @Override
        public void destroy(Integer k) throws IOException {
            if (index > readIndex) {
                body = new byte[size];
                System.arraycopy(buffer, readIndex, body, 0, index - readIndex);
            } else {
                body = new byte[size];
                inputStream.read(body, 0, size);
            }
        }
    }
}
