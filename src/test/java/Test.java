import JSON.JSON;
import Logger.Logger;
import server.Server;
import web.Socket.HttpFactory;
import web.Socket.WebHttpServerFactory;
import web.http.Controller.WebServlet;
import web.http.HttpRequest;
import web.http.HttpResponse;

import java.util.Arrays;
import java.util.List;

class u {
	int id = 1;
}

class pojo {
	int id = 1;
	String name = "陈浩";
	List<u> us = Arrays.asList (new u ( ), new u ( ));
}

/**
 * @author 陈浩
 * @Date: 21/06/02 0:18
 */
public class Test {
	private final static int PORT = 80;
	
	@SuppressWarnings("all")
	public static void main(String[] args) throws Throwable {
		Server<Integer> server = new HttpFactory ( );
		Runtime.getRuntime ( ).addShutdownHook (new Thread (() -> {
			Logger.info ("Service stops on port ".concat (String.valueOf (PORT)));
			server.destroy (-1);
		}));
		final WebServlet webServlet = new WebServlet ( ) {
			@Override
			public void doGet(final HttpRequest request, final HttpResponse response) {
				response.getPrintSteam ( ).println (JSON.ObjectToString (new pojo ( )));
			}
			
			@Override
			public void doPost(final HttpRequest request, final HttpResponse response) {
				doGet (request, response);
			}
		};
		WebHttpServerFactory server1 = (WebHttpServerFactory) server;
		server1.getServletFactory ( ).addContainer ("/api/user/login", webServlet);
		server1.addContainer ((request, response) -> {
			Logger.info ("进入过滤器");
			response.setCharset ("UTF-8");
		});
		server.start (PORT);
	}
}
