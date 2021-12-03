package org.context.Bean;

import java.util.List;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:08 上午 2021/12/2
 * @Modified By:
 */
public interface BeanDefinition<T> {
    void resolvableConstructorDependency() throws NoSuchMethodException;

    void resolvableFieldDependency(List<BeanDefinition> definitions);

    BeanFactory getBeanFactory();

}
