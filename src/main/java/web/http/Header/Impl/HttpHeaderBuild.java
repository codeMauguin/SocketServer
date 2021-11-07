package web.http.Header.Impl;

import server.Server;
import web.http.Header.HttpHeader;

import java.util.HashMap;
import java.util.Map;

public abstract class HttpHeaderBuild implements HttpHeader, Server<Integer> {
	protected final static String SEMICOLON = ":";
	protected Map<String, String> headers;
	
	@Override
	public void start(Integer k) {
		headers = new HashMap<> (k);
	}
	
	@Override
	public void destroy(Integer k) {
		headers.clear ( );
		headers = null;
	}
	
	public void setHeader(String key, String value) {
		headers.put (key, value);
	}
}
