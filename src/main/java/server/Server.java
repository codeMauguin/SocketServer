package server;

public interface Server<key>{
    /**
     * 服务启动
     * @param k
     */
    void start(key k) throws Throwable;

    /**
     * 服务关闭
     */
    void destroy(key k);
}
