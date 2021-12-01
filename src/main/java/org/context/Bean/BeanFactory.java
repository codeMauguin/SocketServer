package org.context.Bean;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 8:53 下午 2021/11/30
 * @Modified By:
 */
public interface BeanFactory<T> {
    T getObject() throws Exception;

    Class<T> getType();
}
