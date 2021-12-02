package MyJSON;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static java.util.Locale.ENGLISH;

public class JSON {
    private final static String TEMPLATE = "{%s}";
    private final static String BODY = "\"{0}\":{1}";
    private final static String SEMICOLON = ",";
    private final static String LIST_TEMPLATE = "[%s]";


    public static String ObjectToString(Object target) {
        if (Objects.isNull(target)) {
            return "null";
        }
        StringBuilder resList = new StringBuilder();
        if (Objects.isNull(target.getClass().getClassLoader()) || Objects.isNull(target.getClass().getClassLoader().getName())) {
            /*
            处理数组类型
             */
            if (target instanceof Iterable<?> iterable) {
                Iterator<?> iterator = iterable.iterator();
                return getString(resList, iterator);
            }
            /*
            处理Map类型
                         */
            else if (target instanceof Map<?, ?> map) {
                Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<?, ?> next = iterator.next();
                    resList.append(MessageFormat.format(BODY, next.getKey(),
                            ObjectToString(next.getValue())));
                    if (iterator.hasNext()) {
                        resList.append(SEMICOLON);
                    }
                }
                return String.format(TEMPLATE, resList);
            } else if (target.getClass().isArray()) {
                Object[] target1 = (Object[]) target;
                int index = 0;
                for (Object object : target1) {
                    resList.append(ObjectToString(object));
                    if (index != target1.length - 1) {
                        resList.append(SEMICOLON);
                    }
                    index++;
                }
                return String.format(LIST_TEMPLATE, resList);
            } else {
                return String.format("\"%s\"", target);
            }
        } else {
            try {
                Iterator<Field> iterator = Arrays.stream(target.getClass().getDeclaredFields()).iterator();
                while (iterator.hasNext()) {
                    Field next = iterator.next();
                    if (next.trySetAccessible()) {
                        String fieldName = next.getName().substring(0, 1).toUpperCase(ENGLISH) + next.getName().substring(1);
                        String IS = "is";
                        String GET = "get";
                        Method method;
                        try {
                            method = target.getClass().getMethod(IS + fieldName);
                        } catch (NoSuchMethodException ignore) {
                            try {
                                method = target.getClass().getMethod(GET + fieldName);
                            } catch (NoSuchMethodException ignores) {
                                continue;
                            }
                        }
                        method.setAccessible(true);
                        Object instance = method.invoke(target);
                        resList.append(MessageFormat.format(BODY, next.getName(), ObjectToString(instance)));
                        if (iterator.hasNext()) {
                            resList.append(SEMICOLON);
                        }
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return String.format(TEMPLATE, resList);
        }
    }

    private static String getString(StringBuilder resList, Iterator<?> iterator) {
        while (iterator.hasNext()) {
            Object next = iterator.next();
            resList.append(ObjectToString(next));
            if (iterator.hasNext()) {
                resList.append(SEMICOLON);
            }
        }
        return String.format(LIST_TEMPLATE, resList);
    }

}
