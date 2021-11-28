package web.http.Libary;

import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public record HttpInfo(String method, String path, String version, Map<String, Object> params) {
    public static HttpInfo getInstance(String http) {
        String[] info = http.split(" ");
        String method = info[0];
        String route = info[1];
        String version = info[2].substring(info[2].indexOf("/") + 1);
        String path;
        Map<String, Object> params;
        final int index = route.indexOf("?");
        if (index > -1) {
            String param = route.substring(index + 1);
            path = route.substring(0,
                    index);
            MultiMap<String> map = new MultiMap<>();
            UrlEncoded.decodeTo(URLDecoder.decode(param, StandardCharsets.UTF_8), map, "UTF-8");
            params = map;
        } else {
            params = new HashMap<>();
            path = route;
        }
        return new HttpInfo(method, path, version, params);
    }

}
