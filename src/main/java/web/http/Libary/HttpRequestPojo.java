package web.http.Libary;

import java.util.Map;
import java.util.Objects;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 22:08 2021/11/6
 * @Modified By:
 */
public final class HttpRequestPojo {
    private final HttpInfo info;
    private String body;

    /**
     *
     */
    public HttpRequestPojo(HttpInfo info) {
        this.info = info;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getParams() {
        return this.info.params();
    }

    public void setParams(Map<String, Object> params) {
        this.info.params().putAll(params);
    }

    public String path() {
        return info.path();
    }

    public String method() {
        return info.method();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HttpRequestPojo) obj;
        return Objects.equals(this.info.path(), that.info.path()) &&
                Objects.equals(this.info.method(), that.info.method());
    }

    @Override
    public int hashCode() {
        return Objects.hash(info.path(), info.method());
    }

    @Override
    public String toString() {
        return "HttpRequestPojo[" +
                "path=" + info.path() + ", " +
                "method=" + info.method() + ']';
    }

}
