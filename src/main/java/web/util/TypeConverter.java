package web.util;

import com.alibaba.fastjson.JSONException;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

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
    public static <T> T primitiveConversion(String message, Class<T> destType) {
        return (T) typeConversionAdapter.getOrDefault(destType,
                typeConversionAdapter.get(primitiveWrapperTypeMap.get(destType))).apply(message);
    }

    public static <T> T typeConversion(String message, Class<T> destType) {
        if (isPrimitive(destType))
            return primitiveConversion(message, destType);
        return null;
    }

    @SuppressWarnings("all")
    public static <T> T typeCollectionConversion(Parameter parameter, MessageReader.lexec exec) {
        ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Collection collection = createCollection(parameterizedType);
        List<MessageReader.lexec> lexecs = MessageReader.readList(exec);
        for (MessageReader.lexec lexec : lexecs) {
            collection.add(
                    typeConversion(lexec, genericType));
        }
        return (T) collection;
    }

    @SuppressWarnings("all")
    public static <T> T typeMapConversion(Parameter parameter, MessageReader.lexec exec) {
        ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
        Map map = new HashMap();
        MessageReader reader = new MessageReader(exec.readAllBytes());
        Map<String, MessageReader.lexec> read = reader.read();
        for (Map.Entry<String, MessageReader.lexec> entry : read.entrySet()) {
            map.put(typeConversion(new MessageReader.lexec(entry.getKey().getBytes(StandardCharsets.UTF_8)), keyType),
                    typeConversion(entry.getValue(), valueType));
        }
        return (T) map;
    }


    private static <T> T typeConversion(MessageReader.lexec lexec, Class<T> genericType) {
        if (isPrimitive(genericType)) {
            return typePrimitiveConversion(lexec, genericType);
        } else {
            return null;
        }
    }

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
