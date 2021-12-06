package web.server;

import Logger.Logger;
import org.context.Bean.DefaultSingletonBeanRegistry;
import org.reflections.Reflections;
import web.Socket.HttpNioServer;
import web.Socket.WebHttpServerFactory;
import web.http.Controller.UtilScan;
import web.http.Filter.FilterRecord;
import web.http.Libary.ControllerRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.*;


/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:05 下午 2021/11/29
 * @Modified By:
 */
public class WebWorkServer implements WebServer {
    private final Set<Reflections> reflections = new HashSet<>();
    private WebHttpServerFactory webHttpServerFactory;

    private WebServerContext init(String[] args) throws Throwable {
        WebServerContext context = null;
        if (args.length == 0) {
            Logger.info("args is null");
            exit(0);
        } else {
            context = initContext(args[0]);
            String[] path = new String[args.length - 1];
            arraycopy(args, 1, path, 0, path.length);
            initReflections(path);
        }
        initBean(context);
        initFilter(context);
        initController(context);
        initServlet(context);
        initServer();
        return context;
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

    private WebServerContext initContext(String arg) throws UnknownHostException {
        int indexOf = arg.lastIndexOf(":");
        int port;
        InetAddress ip;
        if (indexOf > -1) {
            port = Integer.parseInt(arg.substring(indexOf + 1));
            ip = InetAddress.getByName(arg.substring(0, indexOf));
        } else {
            port = Integer.parseInt(arg);
            ip = InetAddress.getByName("0.0.0.0");
        }
        return new WebServerContext(port, ip);
    }


    @Override
    public void run(String[] args) {
        try {
            long start = currentTimeMillis();
            WebServerContext context = init(args);
            context.setStart(start);
            start(context);
            gc();
        } catch (Throwable e) {
            Logger.warn(e.getMessage());
        }
    }


    @Override
    public void start(WebServerContext context) throws Throwable {
        this.webHttpServerFactory.start(context);
    }

    @Override
    @Deprecated
    public void destroy(WebServerContext k) {
    }
}
