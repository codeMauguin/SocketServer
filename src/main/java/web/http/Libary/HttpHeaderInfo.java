package web.http.Libary;

public class HttpHeaderInfo {
    private final HttpInfo info;
    private String type = "";
    private String origin = "";
    private String charset = "UTF-8";
    private int length = 0;
    private String timeout;

    private boolean isConnection = true;

    public HttpHeaderInfo(HttpInfo info) {
        this.info = info;
    }

    public boolean isConnection() {
        return isConnection;
    }

    public void setConnection(boolean connection) {
        isConnection = connection;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getMethod() {
        return info.method();
    }

    public String getVersion() {
        return info.version();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public HttpInfo getInfo() {
        return info;
    }
}
