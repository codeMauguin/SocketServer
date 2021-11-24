package web.http.Libary;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 22:08 2021/11/6
 * @Modified By:
 */
public final class HttpRequestPojo {
    private final String path;
    private final String method;
    private final Map<String, Object> params;

    private String body;

    /**
     *
     */
    public HttpRequestPojo(String path, String method) {
        this.path = path;
        this.method = method;
        params = new HashMap<>();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params.putAll(params);
    }

    public String path() {
        return path;
    }

    public String method() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HttpRequestPojo) obj;
        return Objects.equals(this.path, that.path) &&
                Objects.equals(this.method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }

    @Override
    public String toString() {
        return "HttpRequestPojo[" +
                "path=" + path + ", " +
                "method=" + method + ']';
    }

}
