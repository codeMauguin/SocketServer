package web.http;

import java.io.InputStream;

public interface HttpRequest {
    String getHeader(String key);

    String getPath();

    String getMethod();

    InputStream getInputStream();


}
