package web.http.Imlp;

import com.alibaba.fastjson.JSON;
import web.http.Header.HttpHeader;
import web.http.Header.Impl.HttpHeaderBuild;
import web.http.HttpRequest;
import web.http.Libary.HttpRequestPojo;

import java.io.InputStream;

public class HttpServletRequest implements HttpRequest {
    private final HttpHeader httpHeader;
    private final InputStream inputStream;
    private final HttpRequestPojo httpRequestPojo;

    public HttpServletRequest(final InputStream inputStream, final HttpHeaderBuild h, final HttpRequestPojo httpRequestPojo) {
        this.inputStream = inputStream;
        this.httpHeader = h;
        this.httpRequestPojo = httpRequestPojo;
    }

    @Override
    public String getHeader(String key) {
        return httpHeader.get(key);
    }

    @Override
    public String getPath() {
        return httpRequestPojo.path();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public Object getParam(String name) {
        return httpRequestPojo.getParams().get(name);
    }

    @Override
    public <T> T getParam(Class<T> src) {
        String body = httpRequestPojo.getBody();
        if (body != null) {
            return JSON.parseObject(body, src);
        }
        return null;
    }

}
