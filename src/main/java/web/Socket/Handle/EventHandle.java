package web.Socket.Handle;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:24 下午 2021/12/5
 * @Modified By:
 */
public interface EventHandle<T> {
    void handle(T t);
}
