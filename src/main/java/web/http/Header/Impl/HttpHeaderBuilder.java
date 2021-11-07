package web.http.Header.Impl;

import java.util.Locale;
import java.util.Map;

public class HttpHeaderBuilder extends HttpHeaderBuild {
	public HttpHeaderBuilder() {
		try {
			super.start (3);
		} catch (Throwable throwable) {
			throwable.printStackTrace ( );
		}
	}
	
	@Override
	public String get(String key) {
		for (Map.Entry<String, String> stringStringEntry : this.headers.entrySet ( )) {
			if (stringStringEntry.getKey ( ).toLowerCase (Locale.ROOT).equals (key.toLowerCase (Locale.ROOT))) {
				return stringStringEntry.getValue ( );
			}
		}
		return null;
	}
}
