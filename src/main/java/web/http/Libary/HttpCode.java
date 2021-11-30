package web.http.Libary;

/**
 * @author 陈浩
 * @Date: created in 18:20 2021/11/6
 * @Modified By:
 */
public enum HttpCode {
    HTTP_100(100, "Continue"),
    HTTP_200(200, "OK"),
    HTTP_201(201, "Created"),
    HTTP_202(202, "Accepted"),
    HTTP_404(404, "Not Found"),
    HTTP_405(405, "Method not allowed");
    private final int code;
    private final String msg;

    HttpCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}