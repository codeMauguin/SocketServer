package web.http.Controller;

import web.http.Libary.Container;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

record container(String path, WebServlet servlet) {
	container(final String path, final WebServlet servlet) {
		Pattern pattern = Pattern.compile ("^/");
		this.path = pattern.matcher (path).find ( ) ? path : String.format ("/%s", path);
		this.servlet = servlet;
	}
	
	@Override
	public boolean equals(final Object o) {
		final String[] source = ((String) o).split ("/");
		final String[] target = path.split ("/");
		return Arrays.equals (source, target);
	}
}

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 20:49 2021/11/6
 * @Modified By:
 */
public class ServletFactory implements Container<WebServlet, String> {
	private final List<container> servletContainer;
	
	public ServletFactory() {
		this.servletContainer = new LinkedList<> ( );
	}
	
	@Override
	public void addContainer(final WebServlet webServlet) {
	}
	
	@Override
	public void addContainer(String path, final WebServlet webServlet) {
		//处理路径
		servletContainer.add (new container (path, webServlet));
	}
	
	public WebServlet getServlet(String path) {
		return servletContainer.stream ( ).filter (r -> r.equals (path)).findFirst ( ).map (container::servlet).orElse (null);
	}
}
