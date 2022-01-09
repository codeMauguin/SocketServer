package web.Socket.Handle;

import web.http.HttpResponse;
import web.http.Imlp.MultiValueMap;
import web.http.Imlp.MultiValueMapAdapter;
import web.http.Libary.HttpCode;
import web.http.Libary.HttpHeaderInfo;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

public class HttpServletResponse implements HttpResponse {
    protected final MultiValueMap<String, String> headers;
    private final OutputStream outputStream;
    private final PrintStream printSteam;
    private final HttpHeaderInfo httpHeader;
    private String response_unicode = "UTF-8";
    private HttpCode code = HttpCode.HTTP_200;

    HttpServletResponse(OutputStream outputStream, HttpHeaderInfo httpHeader) {
        this.outputStream = outputStream;
        printSteam = new PrintStream(outputStream);
        headers = new MultiValueMapAdapter<>();
        headers.add("Date", LocalDateTime.now().toString());
        headers.add("Connection", "keep-alive");
        headers.add("Keep-Alive", "timeout=" + httpHeader.getTimeout());
        this.httpHeader = httpHeader;
    }

    public String getMethod() {
        return httpHeader.getMethod();
    }

    public HttpCode getCode() {
        return code;
    }

    public void setCode(HttpCode code) {
        this.code = code;
    }

    public String getOrigin() {
        return httpHeader.getOrigin();
    }

    public void setLength(int size) {
        this.headers.add("Content-Length", String.valueOf(size));
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void addHeader(String key, String value) {
        this.headers.add(key, value);
    }

    @Override
    public void setContentType(final String value) {
        this.headers.add("Content-Type", value);
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintStream getPrintSteam() {
        return printSteam;
    }

    @Override
    public void setCharset(String unicode) {
        this.response_unicode = unicode;
    }

    public String getResponseUnicode() {
        return response_unicode;
    }

    public String getVersion() {
        return httpHeader.getVersion();
    }
}
