package org.context.Bean;

import Logger.Logger;
import org.context.Bean.annotation.Resource;
import org.reflections.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.*;

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
    private final Set<BeanFactory<?>> cacheBean = new LinkedHashSet<>();

    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    private final Map<String, List<Fields>> populate = new HashMap<>();

    private final Map<Class<?>, Set<Field>> record = new HashMap<>();

    private final String errorInfo = """

             ***************************
             APPLICATION FAILED TO START
             ***************************

             Description:

            {0}
             """;

    public void registered(Class<?> target, String beanName) {
        try {
            BeanFactory<?> beanFactory = createBeanFactory(target, beanName);
            if (beanFactory == null) {
                Logger.error("bean %s is failed to create".formatted(beanName));
                return;
            }
            if (((SingleBeanFactory<?>) beanFactory).isDefault()) {
                Object object = beanFactory.getObject();
                earlySingletonObjects.put(beanName, object);
                populateBean(beanName, target, object);
                check(target, object);
            } else {
                cacheBean.add(beanFactory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> create() throws Exception {
        for (BeanFactory<?> beanFactory : cacheBean) {
            Class<?>[] parameters = ((SingleBeanFactory<?>) beanFactory).getParameters();
            create(beanFactory, parameters);
            cacheBean.remove(beanFactory);
        }
        for (Map.Entry<String, List<Fields>> vars : populate.entrySet()) {
            List<Fields> fields = vars.getValue();
            for (Fields field : fields) {
                Object bean = getBean(field.getField().getType());
                if (bean == null) {
                    Logger.error(errorInfo, """
                            bean %s is not found
                            """.formatted(field.getField().getType()));
                    System.exit(0);
                }
                populateBean(field.getField(), field.getTarget(), getBean(field.getTarget()),
                        bean);
            }
        }
        populate.clear();
        return earlySingletonObjects;
    }

    private void create(BeanFactory<?> beanFactory, Class<?>[] parameters) throws Exception {
        SingleBeanFactory singleBeanFactory = (SingleBeanFactory) beanFactory;
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            Class<?> parameter = parameters[i];
            BeanFactory<?> beanFactory1 = cacheBean.stream()
                    .filter(var0 ->
                            var0.getType().equals(parameter)).findFirst().orElse(null);
            if (beanFactory1 != null) {
                if (Arrays.stream(((SingleBeanFactory) beanFactory1).getParameters()).anyMatch(var0 -> var0.equals(beanFactory.getType()))) {
                    Logger.error("circularDependency！");
                    return;
                }
                create(beanFactory1, ((SingleBeanFactory) beanFactory1).getParameters());
            }
            Object bean = getBean(parameter);
            if (bean == null) {
                Logger.error("bean is not found");
                return;
            }
            (singleBeanFactory).fill(i, bean);
        }
        Object object = beanFactory.getObject();
        populateBean(singleBeanFactory.getBeanName(), singleBeanFactory.getType(), object);
        earlySingletonObjects.put(((SingleBeanFactory<?>) beanFactory).getBeanName(), object);
        check(singleBeanFactory.getType(), object);
    }

    private Object getBean(Class<?> parameter) {
        Object var0;
        var0 =
                earlySingletonObjects.entrySet().stream().filter(var1 -> var1.getValue().getClass().equals(parameter)).findFirst().map(Map.Entry::getValue).orElse(null);
        return var0;
    }


    private void check(Class<?> target, Object object) {
        populate.entrySet().stream()
                .filter(stringListEntry -> stringListEntry.getValue().stream().anyMatch(var0 -> var0.getField().getType().equals(target)))
                .map(r -> {
                    Map<String, Fields> fieldsMap = new HashMap<>();
                    fieldsMap.put(r.getKey(),
                            r.getValue().stream().filter(var1 -> var1.getField().getType().equals(target)).findFirst()
                                    .orElse(null));
                    return fieldsMap;
                }).flatMap(stringFieldsMap -> stringFieldsMap.entrySet().stream()).forEach(stringFieldsEntry -> {
                    String key = stringFieldsEntry.getKey();
                    Field value = stringFieldsEntry.getValue().getField();
                    Object o = earlySingletonObjects.get(key);
                    try {
                        record.get(value.getType()).forEach(field -> {
                            if (field.getType().equals(o.getClass())) {

                                Logger.warn(errorInfo, MessageFormat.format("""
                                                The dependencies of some of the beans in the application context form a cycle:

                                                   services defined in file [{0}]
                                                ┌─────┐
                                                |  {1}
                                                ↑     ↓
                                                |  {2}
                                                └─────┘
                                                """, object.getClass(),
                                        object.getClass().getSimpleName(),
                                        field.getType().getSimpleName()));
                                System.exit(100);
                            }
                        });
                        populateBean(value, o.getClass(), o, object);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    List<Fields> fields = populate.get(key);
                    fields.remove(value);
                    populate.replace(key, fields);
                });
    }

    private Object getBean(
            String beanName
    ) {
        Object var0;
        var0 = earlySingletonObjects.get(beanName);
        return var0;
    }

    private void populateBean(String beanName, Class<?> target, Object object) throws Exception {
        Set<Field> fields = ReflectionUtils.getAllFields(target, field -> field.isAnnotationPresent(Resource.class));
        record.put(target, fields);
        List<Fields> var1 = new LinkedList<>();
        for (Field field : fields) {
            Resource annotation = field.getAnnotation(Resource.class);
            Object bean = annotation.value().equals("") ? getBean(field.getType()) : getBean(annotation.value());
            if (bean == null) {
                // 可能没有注册 或者构造器有依赖无法实例
                var1.add(new Fields(field, target));
                continue;
            }
            populateBean(field, target, object, bean);
        }
        populate.put(beanName, var1);
    }

    private void populateBean(Field field, Class<?> target, Object object, Object bean) throws Exception {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), target);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(object, bean);
        } catch (Exception ignore) {
            field.setAccessible(true);
            field.set(object, bean);
        }
    }

    private BeanFactory<?> createBeanFactory(Class<?> target, String beanName) {
        Constructor<?>[] constructors = target.getConstructors();
        if (constructors.length == 1) {
            return new SingleBeanFactory<>(target, constructors[0].getParameterTypes(), beanName);
        }
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length != 0) {
                return new SingleBeanFactory<>(target, constructors[0].getParameterTypes(), beanName);
            }
        }
        return null;
    }
}
