package web.Socket;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 12:49 下午 2021/12/5
 * @Modified By:
 */
public interface WebSockServer extends Runnable {

    void start();

    void destroy();

    @Override
    default void run() {
        start();
        destroy();
    }

}
