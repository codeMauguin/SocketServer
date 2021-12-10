package web.server;

import Logger.Logger;
import org.context.Bean.DefaultSingletonBeanRegistry;
import org.reflections.Reflections;
import web.Socket.HttpNioServer;
import web.Socket.WebHttpServerFactory;
import web.http.Controller.UtilScan;
import web.http.Filter.FilterRecord;
import web.http.Libary.ControllerRecord;
import web.util.ConfigReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.exit;
import static java.lang.System.gc;


/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:05 下午 2021/11/29
 * @Modified By:
 */
public class WebWorkServer implements WebServer {
    private final Set<Reflections> reflections = new HashSet<>();
    private WebHttpServerFactory webHttpServerFactory;

    private WebServerContext context;

    private WebWorkServer init(String[] args) throws Throwable {
        List<String> path = new Vector<>();
        if (args.length == 0) {
            Logger.info("args is null");
            exit(0);
        } else {
            AtomicInteger port = new AtomicInteger(80);
            List<String> origins = new Vector<>();
            //Mac 默认的host运行没有权限
            AtomicReference<InetAddress> host = new AtomicReference<>(InetAddress.getByName("0.0.0.0"));
            for (String arg : args) {
                ConfigReader reader = new ConfigReader(arg);
                reader.read((var0, var1) -> {
                    switch (var0) {
                        case "port" -> port.set(Integer.parseInt(var1));
                        case "host" -> {
                            try {
                                host.set(InetAddress.getByName(var1));
                            } catch (UnknownHostException ignore) {
                            }
                        }
                        case "path" -> {
                            path.add(var1);
                        }
                        case "origin" -> {
                            origins.add(var1);
                        }
                    }
                });
            }
            context = initContext(port.get(), host.get());
            context.setOrigins(origins.toArray(String[]::new));
        }
        initReflections(path.toArray(String[]::new));
        return this;
    }

    private void initBean(WebServerContext context) throws Exception {
        DefaultSingletonBeanRegistry registry = new DefaultSingletonBeanRegistry();
        UtilScan.prepareBean(reflections, registry);
        registry.refreshBean();
        registry.initBean(context);
    }


    private void initServlet(WebServerContext context) throws Throwable {
        for (Reflections route : reflections) {
            Set<ControllerRecord> scanController = UtilScan.scanServlet(route);
            context.setControllerRecords(scanController);
        }
    }

    private void initFilter(WebServerContext context) throws Throwable {
        for (Reflections route : reflections) {
            Set<FilterRecord> scanFilter = UtilScan.scanFilter(route);
            context.setFilterRecords(scanFilter);
        }
    }

    private void initReflections(String[] path) {
        for (String route : path) {
            reflections.add(new Reflections(route));
        }
    }

    private void initServer() {
//        this.webHttpServerFactory = new HttpBioServer();
        this.webHttpServerFactory = new HttpNioServer();
    }

    private void initController(WebServerContext context) {
        for (Reflections route : reflections) {
            Set<ControllerRecord> controllerRecords = UtilScan.scanController(route, context);
            context.setControllerRecords(controllerRecords);
        }
    }

    private WebServerContext initContext(int port, InetAddress host) throws UnknownHostException {
        return new WebServerContext(port, host);
    }


    @Override
    public void run(String[] args) {
        try {
            init(args).start(context);
            gc();
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.warn(e.getMessage());
        }
    }


    @Override
    public void start(WebServerContext context) throws Throwable {
        initBean(context);
        initFilter(context);
        initController(context);
        initServlet(context);
        initServer();
        this.webHttpServerFactory.start(context);
    }

    @Override
    @Deprecated
    public void destroy(WebServerContext k) {
    }
}
