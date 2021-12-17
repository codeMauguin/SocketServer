package web.util;

import com.alibaba.fastjson.JSONException;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static java.util.Locale.ENGLISH;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:04 下午 2021/12/7
 * @Modified By:
 */
public class TypeConverter {

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = Map.of(
            Boolean.TYPE, Boolean.class,
            Byte.TYPE, Byte.class,
            Character.TYPE, Character.class,
            Double.TYPE, Double.class,
            Float.TYPE, Float.class,
            Integer.TYPE, Integer.class,
            Long.TYPE, Long.class,
            Short.TYPE, Short.class,
            String.class, String.class
    );
    private final static Map<Class<?>, Function<String, ?>> typeConversionAdapter = Map.of(
            Boolean.class, (Function<String, Boolean>) Boolean::valueOf,
            Character.class, (Function<String, Character>) o -> o.charAt(0),
            Integer.class, (Function<String, Integer>) Integer::valueOf,
            Long.class, (Function<String, Long>) Long::valueOf,
            Byte.class, (Function<String, Byte>) Byte::valueOf,
            Double.class, (Function<String, Double>) Double::valueOf,
            Float.class, (Function<String, Float>) Float::valueOf,
            Short.class, (Function<String, Short>) Short::valueOf,
            String.class, (Function<String, String>) o -> o,
            BigDecimal.class, (Function<String, BigDecimal>) BigDecimal::new
    );

    private final static Map<Class<?>, Function<MessageReader.lexec, ?>> lexecAdapter = Map.of(
            Boolean.class, (Function<MessageReader.lexec, Boolean>) TypeUtils::caseBoolean,
            Character.class, (Function<MessageReader.lexec, Character>) TypeUtils::caseCharacter,
            Integer.class, (Function<MessageReader.lexec, Integer>) TypeUtils::caseInteger,
            Long.class, (Function<MessageReader.lexec, Long>) TypeUtils::caseLong,
            Byte.class, (Function<MessageReader.lexec, Byte>) TypeUtils::caseByte,
            Double.class, (Function<MessageReader.lexec, Double>) TypeUtils::caseDouble,
            Float.class, (Function<MessageReader.lexec, Float>) TypeUtils::caseFloat,
            Short.class, (Function<MessageReader.lexec, Short>) TypeUtils::caseShort,
            String.class, (Function<MessageReader.lexec, String>) o -> new String(o.readAllBytes())
    );


    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || typeConversionAdapter.containsKey(type);
    }

    @SuppressWarnings("all")
    public static <T> T typeBeanConversion(Class<T> type, Map<String, MessageReader.lexec> read) {
        T o = null;
        try {
            Constructor<T> declaredConstructor = type.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            o = declaredConstructor.newInstance();
            for (Field declaredField : type.getDeclaredFields()) {
                Object fieldBean = typeConversion(declaredField, read.get(declaredField.getName()));
                try {
                    Method method =
                            type.getMethod("set" + declaredField.getName().substring(0, 1).toUpperCase(ENGLISH) + declaredField.getName().substring(1),
                                    declaredField.getType());
                    method.setAccessible(true);
                    method.invoke(o, fieldBean);
                } catch (Exception ignore) {
                    declaredField.setAccessible(true);
                    declaredField.set(o, fieldBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    @SuppressWarnings("all")
    public static <T> T typeBeanConversion(Class<T> type, MessageReader.lexec exec) {
        MessageReader reader = new MessageReader(exec.readAllBytes());
        Map<String, MessageReader.lexec> read = reader.read();
        return typeBeanConversion(type, read);
    }


    @SuppressWarnings("all")
    public static <T> T typeBeanConversion(Parameter parameter, MessageReader.lexec exec) {
        Class<T> type = (Class<T>) parameter.getType();
        return typeBeanConversion(type, exec);
    }

    @SuppressWarnings("all")
    private static <T> T typeConversion(Field declaredField, MessageReader.lexec lexec) {
        if (isPrimitive(declaredField.getType())) {
            return (T) typePrimitiveConversion(lexec, declaredField.getType());
        }
        if (Collection.class.isAssignableFrom(declaredField.getType())) {
            Type genericType = declaredField.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                return typeCollectionConversion((ParameterizedType) genericType, lexec);
            } else {
                return typeCollectionConversion((ParameterizedType) null, lexec);
            }
        }
        if (Map.class.isAssignableFrom(declaredField.getType())) {
            Type genericType = declaredField.getGenericType();
            return typeMapConversion((ParameterizedType) genericType, new MessageReader(lexec.readAllBytes()).read());
        }
        return (T) typeBeanConversion(declaredField.getType(), lexec);
    }

    @SuppressWarnings("all")
    public static <T> T typeArrayConversion(Parameter parameter, MessageReader.lexec exec) {
        List<MessageReader.lexec> lexecs = MessageReader.readList(exec);
        Object array = Array.newInstance(parameter.getType().componentType(), lexecs.size());
        for (int i = 0; i < lexecs.size(); i++) {
            MessageReader.lexec lexec = lexecs.get(i);
            Array.set(array, i, typePrimitiveConversion(lexec, parameter.getType().componentType()));
        }
        return (T) array;
    }

    @SuppressWarnings("all")
    private static <T> T typeCollectionConversion(ParameterizedType type, MessageReader.lexec exec) {
        Collection collection = createCollection(type);
        Type actualTypeArgument = type.getActualTypeArguments()[0];
        Class<?> genericType = type == null ? Object.class : (Class<?>) type.getActualTypeArguments()[0];
        List<MessageReader.lexec> lexecs = MessageReader.readList(exec);
        for (MessageReader.lexec lexec : lexecs) {
            collection.add(
                    typeConversion(lexec, genericType));
        }
        return (T) collection;
    }

    @SuppressWarnings("all")
    public static <T> T typeCollectionConversion(Parameter parameter, MessageReader.lexec exec) {
        ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        return typeCollectionConversion(parameterizedType, exec);

    }

    @SuppressWarnings("all")
    private static <T> T typeMapConversion(ParameterizedType parameterizedType, Map<String, MessageReader.lexec> read) {
        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
        Map map = new HashMap();
        for (Map.Entry<String, MessageReader.lexec> entry : read.entrySet()) {
            map.put(typeConversion(new MessageReader.lexec(entry.getKey().getBytes(StandardCharsets.UTF_8)), keyType),
                    typeConversion(entry.getValue(), valueType));
        }
        return (T) map;
    }

    @SuppressWarnings("all")
    public static <T> T typeMapConversion(Parameter parameter, Map<String, MessageReader.lexec> read) {
        ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        return typeMapConversion(parameterizedType, read);
    }


    /**
     * 不允许List,Map对象。因为泛型检测不到，目前知识不够
     *
     * @param lexec       解析器
     * @param genericType 目标类型
     * @param <T>         返回类型
     * @return 实例对象
     */
    private static <T> T typeConversion(MessageReader.lexec lexec, Class<T> genericType) {
        if (isPrimitive(genericType)) {
            return typePrimitiveConversion(lexec, genericType);
        } else {
            return typeBeanConversion(genericType, lexec);
        }
    }

    @SuppressWarnings("all")
    public static <T> T typePrimitiveConversion(MessageReader.lexec lexec, Class<T> type) {
        Class<?> destType;
        if (type.isPrimitive())
            destType = primitiveWrapperTypeMap.get(type);
        else
            destType = type;
        return (T) lexecAdapter.get(destType).apply(lexec);
    }

    @SuppressWarnings("all")
    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 1) {
                return getRawClass(upperBounds[0]);
            } else {
                throw new JSONException("TODO");
            }
        } else {
            throw new JSONException("TODO");
        }
    }


    @SuppressWarnings("all")
    private static Collection createCollection(Type type) {
        Class<?> rawClass = getRawClass(type);
        Object list;
        if (rawClass != AbstractCollection.class && rawClass != Collection.class) {
            if (rawClass.isAssignableFrom(HashSet.class)) {
                list = new HashSet();
            } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
                list = new LinkedHashSet();
            } else if (rawClass.isAssignableFrom(TreeSet.class)) {
                list = new TreeSet();
            } else if (rawClass.isAssignableFrom(ArrayList.class)) {
                list = new ArrayList();
            } else if (rawClass.isAssignableFrom(EnumSet.class)) {
                Object itemType;
                if (type instanceof ParameterizedType) {
                    itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
                } else {
                    itemType = Object.class;
                }

                list = EnumSet.noneOf((Class) itemType);
            } else if (!rawClass.isAssignableFrom(Queue.class)) {
                try {
                    list = rawClass.newInstance();
                } catch (Exception var4) {
                    throw new JSONException("create instance error, class " + rawClass.getName());
                }
            } else {
                list = new LinkedList();
            }
        } else {
            list = new ArrayList();
        }
        return (Collection) list;
    }
}
