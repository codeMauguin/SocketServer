package org.context.Bean;

import Logger.Logger;
import web.server.WebServerContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Locale.ENGLISH;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 8:54 下午 2021/11/30
 * @Modified By:
 */
public class DefaultSingletonBeanRegistry {
    /*
    有构造器依赖 最后创造实例
     */
    private final Set<BeanDefinition> definitions = new HashSet<>();

    private final Set<BeanDefinition> definitionPool = new HashSet<>();

    private final String errorInfo = """

             ***************************
             APPLICATION FAILED TO START
             ***************************

             Description:

            {0}
             """;

    public void registered(Class<?> target, String beanName) throws NoSuchMethodException {
        BeanFactory factory = new SingleBeanFactory(target, beanName);
        BeanDefinition beanDefinition = new ChildBeanDefinition((FactoryBean) factory);
        beanDefinition.resolvableConstructorDependency();
        definitions.add(beanDefinition);
    }


    public <T> T getBean(String var0) {
        BeanDefinition first = findFirst(definitionPool, beanDefinition -> beanDefinition.getBeanFactory().getBeanName().equals(var0));
        try {
            return Objects.nonNull(first) ? (T) first.getBeanFactory().getObject() : null;
        } catch (Exception ignore) {
        }
        return null;
    }

    public <T> T getBean(Class<T> var0) {
        BeanDefinition first = findFirst(definitionPool,
                beanDefinition -> beanDefinition.getBeanFactory().getType().equals(var0));
        try {
            return Objects.nonNull(first) ? (T) first.getBeanFactory().getObject() : null;
        } catch (Exception ignore) {
        }
        return null;
    }

    public void refreshBean() {
        for (BeanDefinition definition : definitions) {
            definition.resolvableFieldDependency(definitions);
        }
    }

    private <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst().orElse(null);
    }

    private void createBeanInstance(BeanDefinition<?> definition) throws Exception {
        definitions.remove(definition);
        BeanFactory beanFactory = definition.getBeanFactory();
        if (beanFactory.isDefault()) {
            ((FactoryBean) beanFactory).setObject(beanFactory.getObject());
        } else {
            FactoryBean factoryBean = (FactoryBean) beanFactory;
            Class<?>[] constructorParameters = factoryBean.getConstructorParameters();
            for (int i = 0, constructorParametersLength = constructorParameters.length; i < constructorParametersLength; i++) {
                Class<?> constructorParameter = constructorParameters[i];
                BeanDefinition find = findFirst(definitions,
                        beanDefinition -> beanDefinition.getBeanFactory().getType().equals(constructorParameter));
                if (find == null) {
                    find = findFirst(definitionPool,
                            beanDefinition -> beanDefinition.getBeanFactory().getType().equals(constructorParameter));
                    if (find != null) {
                        createBeanInstance(find);
                    } else {
                        Logger.error(errorInfo, "bean %s is not found".formatted(constructorParameter.getSimpleName()));
                        System.exit(0);
                    }
                } else {
                    createBeanInstance(find);
                }
                factoryBean.fill(i, find.getBeanFactory().getObject());
            }
            ((FactoryBean) beanFactory).setObject(beanFactory.getObject());
        }
        definitionPool.add(definition);
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
            try {
                Method method = type.getMethod("set" + key.getName().substring(0, 1).toUpperCase(ENGLISH) + key.getName().substring(1),
                        key.getType());
                method.setAccessible(true);
                method.invoke(beanFactory.getObject(), value.getBeanFactory().getObject());
            } catch (NoSuchMethodException ignore) {
                key.setAccessible(true);
                key.set(beanFactory.getObject(), value.getBeanFactory().getObject());
            }
        }
    }

    private void populateBean() throws Exception {
        for (BeanDefinition beanDefinition : definitionPool) {
            populateBean(beanDefinition);
        }
    }

    private void createBeanInstances() throws Exception {
        while (definitions.size() > 0) {
            List<BeanDefinition> beanDefinitions = definitions.stream().toList();
            BeanDefinition beanDefinition = beanDefinitions.get(beanDefinitions.size() - 1);
            createBeanInstance(beanDefinition);
        }
    }
}
