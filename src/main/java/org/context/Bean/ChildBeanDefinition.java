package org.context.Bean;

import com.whit.Logger.Logger;
import org.context.Bean.annotation.Resource;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:14 上午 2021/12/2
 * @Modified By:
 */
public class ChildBeanDefinition<T> implements BeanDefinition {
    private final FactoryBean factoryBean;
    private final String errorInfo = """

             ***************************
             APPLICATION FAILED TO START
             ***************************

             Description:

            {0}
             """;

    public ChildBeanDefinition(FactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Override
    public void resolvableConstructorDependency() throws NoSuchMethodException {
        Class<?> target = this.factoryBean.getType();
        Constructor[] constructors = target.getDeclaredConstructors();
        Constructor<?> declaredConstructor = constructors[0];
        factoryBean.setConstructor(declaredConstructor);
        factoryBean.setConstructorVars(declaredConstructor.getParameterTypes());
        factoryBean.setDefault(declaredConstructor.getParameterCount() == 0);
    }

    @Override
    public void resolvableFieldDependency(List<BeanDefinition> definitions) {
        Map<Field, BeanDefinition> depends = new LinkedHashMap<>();
        Class<?> target = this.factoryBean.getType();
        Set<Field> fields = ReflectionUtils.getAllFields(target, field -> field.isAnnotationPresent(Resource.class));
        for (Field field : fields) {
            if (!this.factoryBean.check(field.getType())) {
                BeanDefinition definition = definitions.stream().filter(var0 -> var0.getBeanFactory().getType().equals(field.getType())).findFirst()
                        .orElse(null);
                if (definition == null) {
                    Logger.error(errorInfo, """
                            bean %s is not found
                            """.formatted(field.getType()));
                    System.exit(0);
                }
                depends.put(field, definition);
            } else {
                Logger.warn(errorInfo, MessageFormat.format("""
                                The dependencies of some of the beans in the application context form a cycle:

                                   services defined in file [{0}]
                                ┌─────┐
                                |  {1}
                                ↑     ↓
                                |  {2} defined in file [{3}]
                                └─────┘
                                """, target, target.getSimpleName(),
                        field.getType().getSimpleName(), field.getType()));


            }
        }
        factoryBean.setDependency(depends);
    }

    @Override
    public BeanFactory getBeanFactory() {
        return this.factoryBean;
    }
}
