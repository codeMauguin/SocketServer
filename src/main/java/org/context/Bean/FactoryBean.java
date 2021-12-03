package org.context.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:16 上午 2021/12/2
 * @Modified By:
 */
public interface FactoryBean extends BeanFactory {
    void setConstructorVars(Class<?>[] constructorVars);

    void setDependency(Map<Field, BeanDefinition> dependency);

    void fill(int index, Object var);

    void setObject(Object o);

    void init() throws Exception;

    void setDefault(boolean isDefault);

    void setConstructor(Constructor<?> constructor);

    Class<?>[] getConstructorParameters();

}
