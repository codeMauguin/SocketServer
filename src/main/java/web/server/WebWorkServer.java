package web.server;

import org.reflections.Reflections;
import web.Socket.HttpFactory;
import web.Socket.WebHttpServerFactory;
import web.http.Controller.UtilScan;
import web.http.Filter.FilterRecord;
import web.http.Libary.ControllerRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.arraycopy;
import static java.lang.System.exit;

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

    private void init(String[] args) throws Throwable {
        if (args.length == 0) {
            exit(1);
        } else {
            initContext(args[0]);
            String[] path = new String[args.length - 1];
            arraycopy(args, 1, path, 0, path.length);
            initReflections(path);
        }
        initFilter();
        initController();
        initServlet();
        initServer();
    }

    private void initServlet() throws Throwable {
        for (Reflections route : reflections) {
            Set<ControllerRecord> scanController = UtilScan.scanServlet(route);
            context.setControllerRecords(scanController);
        }
    }

    private void initFilter() throws Throwable {
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
        this.webHttpServerFactory = new HttpFactory();
    }

    private void initController() throws Throwable {
        for (Reflections route : reflections) {
            Set<ControllerRecord> controllerRecords = UtilScan.scanController(route);
            context.setControllerRecords(controllerRecords);
        }
    }

    private void initContext(String arg) throws UnknownHostException {
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
        this.context = new WebServerContext(port, ip);
    }


    @Override
    public void run(String[] args) {
        try {
            init(args);
            start(context);
        } catch (Throwable ignore) {
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
