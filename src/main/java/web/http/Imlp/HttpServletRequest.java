package web.http.Imlp;

import web.http.Header.HttpHeader;
import web.http.HttpRequest;
import web.http.Libary.HttpRequestRecord;

import java.io.InputStream;

public class HttpServletRequest implements HttpRequest {
    private final HttpHeader httpHeader;
    private final InputStream inputStream;
    private final HttpRequestRecord httpRequestPojo;

    public HttpServletRequest(final InputStream inputStream, final HttpHeader h, final HttpRequestRecord httpRequestPojo) {
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

    @Override
    public String getMethod() {
        return httpRequestPojo.method();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

}
