package web.server;

import context.Bean.Context;
import web.Socket.Handle.HttpHandle;
import web.Socket.Handle.NioHttpHandle;
import web.http.Filter.FilterRecord;
import web.http.Libary.ControllerRecord;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:05 下午 2021/11/29
 * @Modified By:
 */
public class WebServerContext {
    private final int port;
    private final InetAddress ip;
    private final Set<ControllerRecord> controllerRecords = new HashSet<>();
    private final long start;
    private final Class<? extends HttpHandle> serverType = NioHttpHandle.class;
    private Set<FilterRecord> filterRecords;
    private String[] origins = new String[0];
    private long timeout = 3000;
    private Context beanPools;

    public WebServerContext(int port, InetAddress ip) {
        this.port = port;
        this.ip = ip;
        this.start = System.currentTimeMillis();
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setOrigins(String[] origins) {
        this.origins = origins;
    }

    public boolean checkOrigin(String origin) {
        return Arrays.asList(origins).contains(origin);
    }

    public <T> T getBean(Class<T> var0) {
        return beanPools.getBean(var0);
    }

    public <T> T getBean(String beanName) {
        return beanPools.getBean(beanName);
    }

    public void setBeanPools(Context beanPools) {
        this.beanPools = beanPools;
    }

    public long getStart() {
        return start;
    }

    public List<FilterRecord> getFilter(String path) {
        return filterRecords.stream().filter(filterRecord -> filterRecord.matches(path)).collect(Collectors.toCollection(ArrayList::new));
    }

    protected void setFilterRecords(Set<FilterRecord> filterRecords) {
        this.filterRecords = filterRecords;
    }

    protected void setControllerRecords(Set<ControllerRecord> controllerRecords) {
        this.controllerRecords.addAll(controllerRecords);
    }

    public ControllerRecord getController(String path) {
        return controllerRecords.stream().filter(record -> record.isServlet() ? record.getRegex().equals(path) : Objects.nonNull(record.getMethod(path))).findFirst().orElse(null);
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public Class<? extends HttpHandle> getServerType() {
        return this.serverType;
    }
}
