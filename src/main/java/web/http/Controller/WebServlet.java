package web.http.Controller;

import web.http.HttpRequest;
import web.http.HttpResponse;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 18:43 2021/11/6
 * @Modified By:
 */
public interface WebServlet {
	/**
	 * 处理Get请求
	 *
	 * @param request  请求
	 * @param response 响应
	 */
	void doGet(HttpRequest request, HttpResponse response);
	
	/**
	 * 处理Post请求
	 *
	 * @param request  请求
	 * @param response 响应
	 */
	void doPost(HttpRequest request, HttpResponse response);
}
