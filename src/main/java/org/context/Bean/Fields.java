package org.context.Bean;

import java.lang.reflect.Field;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:48 下午 2021/12/1
 * @Modified By:
 */
public class Fields {
    private final Field field;

    public Field getField() {
        return field;
    }

    public Class<?> getTarget() {
        return target;
    }

    private final Class<?> target;

    public Fields(Field field, Class<?> target) {
        this.field = field;
        this.target = target;
    }
}
