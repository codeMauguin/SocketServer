package web.http;

import java.io.OutputStream;
import java.io.PrintStream;

public interface HttpResponse {
    void addHeader(String key, String value);

    OutputStream getOutputStream();

    PrintStream getPrintSteam();
    void setCharset(String unicode);
}
