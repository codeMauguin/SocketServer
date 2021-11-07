package web.Socket;

import Logger.Logger;
import server.Server;
import web.http.Controller.ServletFactory;
import web.http.Filter.Filter;
import web.http.Libary.Container;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class WebHttpServerFactory implements Server<Integer>, Container<Filter, Integer> {
	protected ThreadPoolExecutor executor;
	protected ServerSocket serverSocket;
	protected List<Filter> filters;
	protected boolean start;
	protected ServletFactory servletFactory;
	
	public WebHttpServerFactory() {
		filters = new ArrayList<> ( );
		servletFactory = new ServletFactory ( );
	}
	
	@Override
	public void addContainer(final Filter filter) {
		filters.add (filter);
	}
	
	@Override
	public void addContainer(Integer index, Filter filter) {
		filters.add (index, filter);
	}
	
	public ServletFactory getServletFactory() {
		return this.servletFactory;
	}
	
	@Override
	public void start(Integer port) throws Throwable {
		executor = new ThreadPoolExecutor (8, 10, 60, TimeUnit.SECONDS, new SynchronousQueue<> ( ),
				
				r -> new Thread (Thread.currentThread ( ).getThreadGroup ( ), r, "web-bio-Server"));
		serverSocket = new ServerSocket (port);
		Logger.info ("http Server start in port " + port);
		start = true;
	}
	
	@Override
	public void destroy(Integer k) {
		executor.shutdown ( );
		executor = null;
		start = false;
	}
}
