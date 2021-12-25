package org.context.Bean;

import com.whit.Logger.Logger;
import web.server.WebServerContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import static java.util.Locale.ENGLISH;
import static web.util.ArraysUtil.findFirst;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 8:54 下午 2021/11/30
 * @Modified By: 陈
 */
public class DefaultSingletonBeanRegistry {
    private final LinkedList<BeanDefinition> definitionQueue = new LinkedList<>();


    @SuppressWarnings("all")
    private final String errorInfo = """

             ***************************
             APPLICATION FAILED TO START
             ***************************

             Description:

            {0}
             """;


    public void registered(Class<?> target, String beanName) throws NoSuchMethodException {
        FactoryBean factory = new SingleBeanFactory(target, beanName);
        BeanDefinition beanDefinition = new ChildBeanDefinition<>(factory);
        beanDefinition.resolvableConstructorDependency();
        definitionQueue.add(beanDefinition);
    }

    @SuppressWarnings("all")
    public <T> T getBean(String var0) {
        BeanDefinition first = findFirst(definitionQueue, beanDefinition -> beanDefinition.getBeanFactory().getBeanName().equals(var0));
        try {
            return Objects.nonNull(first) ? (T) checkScope(first).getObject() : null;
        } catch (Exception ignore) {
        }
        return null;
    }

    private BeanFactory checkScope(BeanDefinition definition) throws Exception {
        BeanFactory beanFactory = definition.getBeanFactory();
        if (!beanFactory.isScope()) {
            if (!beanFactory.isDefault()) {
                Class<?>[] constructorParameters = ((FactoryBean) beanFactory).getConstructorParameters();
                for (int i = 0, constructorParametersLength = constructorParameters.length; i < constructorParametersLength; i++) {
                    Object bean = getBean(constructorParameters[i]);
                    ((FactoryBean) beanFactory).fill(i, bean);
                }
            }
            ((FactoryBean) beanFactory).init();
        }
        populate(definition);
        return definition.getBeanFactory();
    }

    private void populate(BeanDefinition definition) {
        Map<Field, BeanDefinition> definitions = definition.getBeanFactory().getDefinitions();
        for (Map.Entry<Field, BeanDefinition> fieldBeanDefinitionEntry : definitions.entrySet()) {
            Field field = fieldBeanDefinitionEntry.getKey();
            Object bean = getBean(fieldBeanDefinitionEntry.getValue().getBeanFactory().getType());
            try {
                populateField(definition.getBeanFactory().getType(), field, definition.getBeanFactory().getObject(), bean);
            } catch (Exception ignored) {
            }
        }
    }

    private void populateField(Class<?> type, Field key, Object target, Object fieldBean) throws Exception {
        Method method = type.getMethod("set" + key.getName().substring(0, 1).toUpperCase(ENGLISH) + key.getName().substring(1), key.getType());
        try {
            method.setAccessible(true);
            method.invoke(target, fieldBean);
        } catch (Exception ignore) {
            key.setAccessible(true);
            key.set(target, fieldBean);
        }
    }

    @SuppressWarnings("all")
    public <T> T getBean(Class<T> var0) {
        BeanDefinition first = findFirst(definitionQueue, beanDefinition -> beanDefinition.getBeanFactory().getType().isAssignableFrom(var0));
        try {
            return Objects.nonNull(first) ? (T) checkScope(first).getObject() : null;
        } catch (Exception ignore) {
        }
        return null;
    }

    public void refreshBean() {
        for (BeanDefinition definition : definitionQueue) {
            definition.resolvableFieldDependency(definitionQueue);
        }
    }

    private void createBeanInstance(BeanDefinition definition) throws Exception {
        if (definition.getBeanFactory().getObject() == null) {
            BeanFactory beanFactory = definition.getBeanFactory();
            if (beanFactory.isDefault()) {
                ((FactoryBean) beanFactory).init();
            } else {
                FactoryBean factoryBean = (FactoryBean) beanFactory;
                Class<?>[] constructorParameters = factoryBean.getConstructorParameters();
                for (int i = 0, constructorParametersLength = constructorParameters.length; i < constructorParametersLength; i++) {
                    Class<?> constructorParameter = constructorParameters[i];
                    BeanDefinition first = findFirst(definitionQueue, definitions -> definitions.getBeanFactory().getType().isAssignableFrom(constructorParameter));
                    if (first != null) {
                        if (first.getBeanFactory().check(beanFactory.getType())) {
                            Logger.error(errorInfo, MessageFormat.format("""
                                    services defined in file [{0}]
                                    ┌─────┐
                                    |  {1}
                                    ↑     ↓
                                    |  {2} defined in file [{3}]
                                    └─────┘""", beanFactory.getType(), beanFactory.getBeanName(), constructorParameter.getSimpleName(), constructorParameter));
                            System.exit(0);
                        }
                        if (first.getBeanFactory().getObject() == null) {
                            createBeanInstance(first);
                        }
                    } else {
                        Logger.error(errorInfo, "bean %s is not found".formatted(constructorParameter.getSimpleName()));
                        System.exit(0);
                    }
                    factoryBean.fill(i, first.getBeanFactory().getObject());
                }
                ((FactoryBean) beanFactory).init();
            }
        }
    }

    public void initBean(WebServerContext context) throws Exception {
        createBeanInstances();
        populateBean();
        context.setBeanPools(this);
    }

    public void populateBean(BeanDefinition beanDefinition) throws Exception {
        BeanFactory beanFactory = beanDefinition.getBeanFactory();
        Map<Field, BeanDefinition> definitions = beanFactory.getDefinitions();
        for (Map.Entry<Field, BeanDefinition> definitionEntry : definitions.entrySet()) {
            Field key = definitionEntry.getKey();
            BeanDefinition value = definitionEntry.getValue();
            Class<?> type = beanFactory.getType();
            populateField(type, key, beanFactory.getObject(), value.getBeanFactory().getObject());
        }
    }

    private void populateBean() throws Exception {
        for (BeanDefinition beanDefinition : definitionQueue) {
            populateBean(beanDefinition);
        }
    }

    private void createBeanInstances() throws Exception {
        for (BeanDefinition beanDefinition : definitionQueue) {
            createBeanInstance(beanDefinition);
        }
    }
}
