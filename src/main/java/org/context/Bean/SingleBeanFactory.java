package org.context.Bean;

import org.context.Bean.annotation.Scope;
import web.http.Controller.ClassRegistrar;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 9:19 下午 2021/11/30
 * @Modified By:
 */
public class SingleBeanFactory implements FactoryBean {
    private final Class<?> target;
    private String beanName;
    private Class<?>[] parameters;
    private Object[] params;

    private Object instance;
    private boolean isDefault;

    private boolean scope = true;
    private Map<Field, BeanDefinition> definitions;

    public SingleBeanFactory(Class<?> target, String beanName) {
        this(target);
        this.beanName = beanName;
    }

    private SingleBeanFactory(Class<?> target) {
        this.target = target;
        if (target.isAnnotationPresent(Scope.class)) {
            Scope annotation = target.getAnnotation(Scope.class);
            String scope = annotation.value().toLowerCase(Locale.ROOT).trim();
            this.scope = switch (scope) {
                case "prototype", "session" -> false;
                default -> true;
            };
        }
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
    public boolean check(Class<?> target) {
        return Arrays.stream(parameters).anyMatch(var -> var.equals(target));
    }

    @Override
    public Map<Field, BeanDefinition> getDefinitions() {
        return definitions;
    }

    @Override
    public boolean isScope() {
        return scope;
    }

    @Override
    public void init() throws Exception {
        this.instance = ClassRegistrar.registrar(target, parameters, params);
    }

    @Override
    public Object getObject() throws Exception {
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
