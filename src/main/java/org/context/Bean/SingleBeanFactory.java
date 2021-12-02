package org.context.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 9:19 下午 2021/11/30
 * @Modified By:
 */
public class SingleBeanFactory implements FactoryBean {
    private final Class<?> target;
    private final String beanName;
    private Class<?>[] parameters;
    private Object[] params;

    private Object instance;
    private boolean isDefault;
    private Constructor<?> constructor;

    private Map<Field, BeanDefinition> definitions;

    public SingleBeanFactory(Class<?> target, String beanName) {
        this.target = target;
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setConstructorVars(Class<?>[] constructorVars) {
        this.parameters = constructorVars;
        this.params = new Object[constructorVars.length];
    }

    @Override
    public void setDependency(Map<Field, BeanDefinition> dependency) {
        this.definitions = dependency;
    }

    public void fill(int index, Object param) {
        params[index] = param;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public Class<?>[] getConstructorParameters() {
        return this.parameters;
    }

    @Override
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    @Override
    public boolean check(Class<?> target) {
        return Arrays.stream(parameters).anyMatch(var -> var.equals(target));
    }

    @Override
    public Map<Field, BeanDefinition> getDefinitions() {
        return definitions;
    }

    @Override
    public Object getObject() throws Exception {
        if (Objects.isNull(instance)) {
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        }
        return instance;
    }

    @Override
    public void setObject(Object o) {
        this.instance = o;
    }

    @Override
    public Class<?> getType() {
        return target;
    }

}
