package web.Socket;

import Logger.Logger;
import web.http.Controller.Servlet;
import web.http.Controller.ServletFactory;
import web.http.Filter.Filter;
import web.http.Header.Impl.HttpHeaderBuild;
import web.http.Header.Impl.HttpHeaderBuilder;
import web.http.Imlp.HttpServletRequest;
import web.http.Imlp.HttpServletResponse;
import web.http.Libary.HttpCode;
import web.http.Libary.HttpRequestPojo;
import web.http.Libary.NetworkLibrary;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陈浩
 */
public record SockAccept(Socket accept, List<Filter> filters, ServletFactory factory) implements Runnable {
	@Override
	public void run() {
		HttpHeaderBuild h = new HttpHeaderBuilder ( );
		try {
			InputStream inputStream = accept.getInputStream ( );
			OutputStream outputStream = accept.getOutputStream ( );
			String method = null;
			String path = null;
			int code;
			
			BufferedReader bufferedReader = new BufferedReader (new InputStreamReader (inputStream));
			/**
			 * 读取信息
			 */
			String http = bufferedReader.readLine ( );
			Logger.info (String.format ("http:%s", http));
			if (Objects.isNull (http)) {
				return;
			}
			method = http.split (" ")[0];
			path = http.split (" ")[1];
			final int index = path.indexOf ("?");
			if (index > -1) {
				path = path.substring (0,
						index);
			}
			Logger.info (String.format ("method:%s", method));
			Logger.info (String.format ("path:%s", path));
			/**
			 * 	读取请求头
			 */
			while (bufferedReader.ready ( )) {
				String line = bufferedReader.readLine ( );
				if ("".equals (line)) {
					break;
				}
				String[] split = line.split (":");
				switch (split[0].trim ( )) {
					case "Content-Type": {
						Logger.info ("Content-Type:{0}", split[1].trim ( ));
					}
					break;
					case "Content-Length": {
						Logger.info ("Content-Length:{0}", split[1].trim ( ));
					}
				}
				h.setHeader (split[0].trim ( ), split[1].trim ( ));
			}
			HttpServletRequest request = new HttpServletRequest (inputStream, h, new HttpRequestPojo (path, method));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream ( );
			HttpServletResponse response = new HttpServletResponse (byteArrayOutputStream);
			/**
			 * 处理过滤
			 */
			for (Filter filter : filters) {
				filter.doFilter (request, response);
			}
			
			char[] s = new char[1024];
			int read;
			String param = "";
			if (bufferedReader.ready ( ) && (read = bufferedReader.read (s)) > -1) {
				param += URLDecoder.decode (new String (s, 0, read));
			}
			Logger.info ("param:{0}", param);
			/**
			 * 处理请求控制器
			 */
			final Servlet servlet = factory.getServlet (path);
			if (servlet != null)
				switch (method) {
					case "GET": {
						servlet.doGet (request, response);
					}
					break;
					case "POST": {
						servlet.doPost (request, response);
					}
					break;
				}
			else {
				//报错
				response.setCharset ("UTF-8");
				response.getPrintSteam ( ).println ("{\"code\":\"404\",\"msg\":\"页面找不到\"}");
			}
			outputStream.write (String.format (NetworkLibrary.HTTP_HEADER.getContent ( ), "1.1",
					HttpCode.HTTP_200.getCode ( ),
					HttpCode.HTTP_200.getMsg ( )).getBytes (response.getResponseUnicode ( )));
			Map<String, String> headers = response.getHeaders ( );
			for (Map.Entry<String, String> entry : headers.entrySet ( )) {
				String k = entry.getKey ( );
				String v = entry.getValue ( );
				outputStream.write (String.format (NetworkLibrary.HTTP_HEADER_MODEL.getContent ( ), k.trim ( ), v.trim ( )).getBytes (response.getResponseUnicode ( )));
			}
			/**
			 * 输出换行
			 */
			outputStream.write (NetworkLibrary.CRLF.getContent ( ).getBytes ( ));
			outputStream.write (byteArrayOutputStream.toByteArray ( ));
			outputStream.flush ( );
			outputStream.close ( );
		} catch (IOException e) {
			e.printStackTrace ( );
		}
		
	}
}
