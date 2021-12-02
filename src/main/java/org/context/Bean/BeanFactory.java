package org.context.Bean;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 8:53 下午 2021/11/30
 * @Modified By:
 */
public interface BeanFactory {
    Object getObject() throws Exception;

    String getBeanName();

    Class<?> getType();

    boolean isDefault();

    boolean check(Class<?> target);

    Map<Field, BeanDefinition> getDefinitions();

}
