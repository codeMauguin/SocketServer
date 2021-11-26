package web.Socket;

import Logger.Logger;
import com.alibaba.fastjson.JSON;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;
import web.http.Controller.Servlet;
import web.http.Controller.ServletFactory;
import web.http.Filter.Filter;
import web.http.Header.Impl.HttpHeaderBuild;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.Imlp.HttpServletRequest;
import web.http.Imlp.HttpServletResponse;
import web.http.Libary.HttpCode;
import web.http.Libary.HttpRequestPojo;
import web.http.Libary.NetworkLibrary;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author 陈浩
 */
public record SockAccept(Socket accept, List<Filter> filters, ServletFactory factory) implements Runnable {
    private final static Pattern JSON_MATCH = Pattern.compile(".*json.*", Pattern.CASE_INSENSITIVE);
    private final static Pattern FORM_MATCH = Pattern.compile(".*x-www-form-urlencoded.*", Pattern.CASE_INSENSITIVE);

    @Override
    public void run() {
        try {
            InputStream inputStream = accept.getInputStream();
            OutputStream outputStream = accept.getOutputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            accept.setKeepAlive(true);
            while (accept.isConnected() && !accept.isClosed()) {
                HttpHeaderBuild h = new HttpHeaderBuilder();
                Logger.info(String.valueOf(accept.getPort()));
                accept.setSoTimeout(3000);
                String method;
                String path;
                String type = "";
                String origin = "";
                String charset = "UTF-8";
                String version;
                int length = 0;
                Map<String, Object> params = new HashMap<>();
                /*
                  读取信息
                 */
                String http = bufferedReader.readLine();
                Logger.info(String.format("http:%s", http));
                if (Objects.isNull(http)) {
                    /*
                       如果读取为空此次请求错误
                     */
                    break;
                } else {
                    String[] info = http.split(" ");
                    method = info[0];
                    path = info[1];
                    version = info[2].substring(info[2].indexOf("/") + 1);
                    final int index = path.indexOf("?");
                    if (index > -1) {
                        String param = path.substring(index + 1);
                        path = path.substring(0,
                                index);
                        MultiMap<String> map = new MultiMap<>();
                        UrlEncoded.decodeTo(URLDecoder.decode(param, StandardCharsets.UTF_8), map, "UTF-8");
                        params = map;
                        Logger.info(String.format("URL-Param:%s", params));

                    }
                }
                Logger.info(String.format("method:%s", method));
                Logger.info(String.format("path:%s", path));
                /*
                  	读取请求头
                 */
                while (bufferedReader.ready()) {
                    String line = bufferedReader.readLine();
                    if ("".equals(line)) {
                        break;
                    }
                    int split = line.indexOf(":");
                    String key = line.substring(0, split);
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
                                charset = value.substring(start, end);
                                Logger.info("charset:{0}", charset);
                            }
                            if (JSON_MATCH.matcher(value).matches()) {
                                type = "JSON";
                            } else if (FORM_MATCH.matcher(value).matches()) {
                                type = "FORM";
                            }
                        }
                        case "Content-Length" -> {
                            Logger.info("Content-Length:{0}", value);
                            length = Integer.parseInt(value);
                        }
                        case "Origin" -> {
                            origin = value;
                            System.out.println("value = " + value);
                        }
                    }
                    h.setHeader(key, value);
                }
                if (Objects.equals(method.trim(), "OPTIONS")) {
                    optionHandle(outputStream, version, origin);
                    continue;
                }
                HttpRequestPojo requestPojo = new HttpRequestPojo(path, method);
                requestPojo.setParams(params);
                HttpServletRequest request = new HttpServletRequest(inputStream, h, requestPojo);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HttpServletResponse response = new HttpServletResponse(byteArrayOutputStream);
                if (!origin.equals(""))
                    response.setOrigin(origin);
                /*
                 * 处理过滤
                 */
                for (Filter filter : filters) {
                    filter.doFilter(request, response);
                }
                int read;
                if (length > 0) {
                    String body;
                    char[] s = new char[length];
                    read = bufferedReader.read(s);
                    body = URLDecoder.decode(new String(s, 0, read), charset);
                    requestPojo.setBody(body);
                    switch (type) {
                        case "JSON" -> {
                            Map<String, Object> map = JSON.parseObject(body);
                            requestPojo.setParams(map);
                            Logger.info("body:{0}", body);
                        }
                        case "FORM" -> {
                            MultiMap<String> map = new MultiMap<>();
                            UrlEncoded.decodeTo(body, map, charset);
                            requestPojo.setParams(map);
                            Logger.info("body:{0}", map.get("id"));
                        }
                        default -> {
                        }
                    }
                }
                /*
                 * 处理请求控制器
                 */
                final Servlet servlet = factory.getServlet(path);
                if (!(servlet == null || method.trim().equals("OPTIONS")))
                    switch (method) {
                        case "GET" -> servlet.doGet(request, response);
                        case "POST" -> servlet.doPost(request, response);
                    }
                else {
                    //报错
                    response.setCharset("UTF-8");
                    response.getPrintSteam().println("{\"code\":\"404\",\"msg\":\"页面找不到\"}");
                }
                outputStream.write(String.format(NetworkLibrary.HTTP_HEADER.getContent(), version,
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
            }
        } catch (IOException e) {
            try {
                accept.close();
            } catch (IOException ignored) {
            }
            Logger.info(accept.getPort() + "已经断开链接");
        }

    }

    private void optionHandle(OutputStream outputStream, String version, String origin) throws IOException {
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
        headers.put("Keep-Alive", "timeout=3");
        headers.put("Connection", "keep-alive");
        printHead(headers, "UTF-8", outputStream);
        outputStream.write(NetworkLibrary.CRLF.getContent().getBytes());
    }

    private void printHead(Map<String, String> headers, String unicode, OutputStream outputStream) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            try {
                outputStream.write(String.format(NetworkLibrary.HTTP_HEADER_MODEL.getContent(), k.trim(), v.trim()).getBytes(unicode));
            } catch (IOException ignored) {
            }
        }
    }
}
