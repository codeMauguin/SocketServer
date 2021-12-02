package org.context.Bean;

import java.util.Set;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:08 上午 2021/12/2
 * @Modified By:
 */
public interface BeanDefinition<T> {
    void resolvableConstructorDependency() throws NoSuchMethodException;

    void resolvableFieldDependency(Set<BeanDefinition> definitions);

    BeanFactory getBeanFactory();

}
