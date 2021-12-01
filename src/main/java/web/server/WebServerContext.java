package web.server;

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
    private long start;
    private Set<FilterRecord> filterRecords;

    private Map<String, Object> beanPools;

    public WebServerContext(int port, InetAddress ip) {
        this.port = port;
        this.ip = ip;
    }

    public <T> T getBean(Class<T> var0) {
        return (T) beanPools.entrySet().stream().filter(var1 -> var1.getValue().getClass().equals(var0)).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    public <T> T getBean(String beanName) {
        return (T) beanPools.get(beanName);
    }

    public void setBeanPools(Map<String, Object> beanPools) {
        this.beanPools = beanPools;
    }

    public long getStart() {
        return start;
    }

    protected void setStart(long start) {
        this.start = start;
    }

    public Set<FilterRecord> getFilter(String path) {
        return filterRecords.stream()
                .filter(filterRecord -> filterRecord.matches(path))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    protected void setFilterRecords(Set<FilterRecord> filterRecords) {
        this.filterRecords = filterRecords;
    }

    protected void setControllerRecords(Set<ControllerRecord> controllerRecords) {
        this.controllerRecords.addAll(controllerRecords);
    }

    public ControllerRecord getController(String path) {
        return controllerRecords.stream()
                .filter(record -> Objects.nonNull(record.getMethod(path)))
                .findFirst()
                .orElse(null);
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }
}
