package web.http.Header.Impl;

import web.http.Header.HttpHeader;

import java.util.Locale;
import java.util.Map;

public class HttpHeaderBuilder implements HttpHeader {
    private final Map<String, String> headers;

    public HttpHeaderBuilder(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String get(String key) {
        return this.headers.entrySet().stream().filter(stringStringEntry -> stringStringEntry.getKey().toLowerCase(Locale.ROOT).equals(key.toLowerCase(Locale.ROOT))).findFirst().map(Map.Entry::getValue).orElse(null);
    }
}
