package web.http;

import java.io.InputStream;

public interface HttpRequest {
    String getHeader(String key);

    String getPath();

    InputStream getInputStream();

    Object getParam(String name);

    <T> T getParam(Class<T> src);
}
