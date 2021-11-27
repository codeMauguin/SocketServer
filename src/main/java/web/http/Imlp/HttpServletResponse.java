package web.http.Imlp;

import web.http.HttpResponse;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HttpServletResponse implements HttpResponse {
    protected final Map<String, String> headers;
    private final OutputStream outputStream;
    private final PrintStream printSteam;
    private String origin = "";
    private String response_unicode = "UTF-8";

    public HttpServletResponse(OutputStream outputStream,String timeout) {
        this.outputStream = outputStream;
        printSteam = new PrintStream(outputStream);
        headers = new HashMap<>(Map.of(
                "Date", LocalDateTime.now().toString(), "Content-Type",
                "application/json;charset=%s".formatted(response_unicode),
                "Connection", "keep-alive",
                "Keep-Alive", "timeout="+timeout)
        );
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setLength(int size) {
        this.headers.put("Content-Length", String.valueOf(size));
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    @Override
    public void setContentType(final String value) {
        this.headers.put("Content-Type", value);
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
}
