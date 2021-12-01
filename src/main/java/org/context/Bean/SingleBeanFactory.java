package org.context.Bean;

import java.lang.reflect.Constructor;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 9:19 下午 2021/11/30
 * @Modified By:
 */
public class SingleBeanFactory<T> implements BeanFactory<T> {
    private final Class<T> target;
    private final Class<?>[] parameters;
    private final Object[] params;

    private final String beanName;

    public SingleBeanFactory(Class<T> target, Class<?>[] parameters, String beanName) {
        this.target = target;
        this.parameters = parameters;
        params = new Object[parameters.length];
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    protected void fill(int index, Object param) {
        params[index] = param;
    }

    protected boolean isDefault() {
        return parameters.length == 0;
    }

    @Override
    public T getObject() throws Exception {
        Constructor<T> constructor = target.getDeclaredConstructor(parameters);
        constructor.setAccessible(true);
        return constructor.newInstance(params);
    }

    @Override
    public Class<T> getType() {
        return target;
    }

    public Class<?>[] getParameters() {
        return parameters;
    }
}
