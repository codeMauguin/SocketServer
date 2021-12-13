package web.http.Libary;

public record HttpInfo(String method, String path, String version, String params) {
    public static HttpInfo getInstance(String http) {
        String[] info = http.split(" ");
        String method = info[0];
        String route = info[1];
        String version = info[2].substring(info[2].indexOf("/") + 1);
        String path;
        String params;
        final int index = route.indexOf("?");
        if (index > -1) {
            params = route.substring(index + 1);
            path = route.substring(0,
                    index);
        } else {
            params = null;
            path = route;
        }
        return new HttpInfo(method, path, version, params);
    }

}
