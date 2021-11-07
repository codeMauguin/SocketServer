package web.http.Imlp;

import web.http.HttpResponse;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Map;

public class HttpServletResponse implements HttpResponse {
    protected final Map<String, String> headers;
    private final OutputStream outputStream;
    private final PrintStream printSteam;
    private String response_unicode = "UTF-8";

    public HttpServletResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
        printSteam = new PrintStream(outputStream);
        headers = Map.of("Date", LocalDateTime.now().toString(), "Content-Type", "application/json;" +
                "charset=UTF-8;","Access-Control-Allow-Origin","http://localhost:63342");
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
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

    public String getResponse_unicode() {
        return response_unicode;
    }
}
