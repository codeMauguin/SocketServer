package web.http.Imlp;

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

}
