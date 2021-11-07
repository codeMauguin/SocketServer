package web.http.Libary;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 20:47 2021/11/6
 * @Modified By:
 */
public interface Container<T,R> {
    void addContainer(T t);
    void addContainer(R index,T t);
    
}
