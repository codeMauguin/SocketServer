package web.http.Libary;

public enum NetworkLibrary {
	HTTP_HEADER("HTTP/%s %d %s\r\n"),
	HTTP_HEADER_MODEL("%s:%s\r\n"),
	CRLF("\r\n")
	;
	private final String content;
	
	public String getContent() {
		return content;
	}
	
	NetworkLibrary(final String content) {
		this.content = content;
	}
}
