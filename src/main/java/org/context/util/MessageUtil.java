package org.context.util;

import com.alibaba.fastjson.JSON;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 5:06 下午 2021/12/3
 * @Modified By:
 */
public class MessageUtil {

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(9);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, Boolean.TYPE);
        primitiveWrapperTypeMap.put(Byte.class, Byte.TYPE);
        primitiveWrapperTypeMap.put(Character.class, Character.TYPE);
        primitiveWrapperTypeMap.put(Double.class, Double.TYPE);
        primitiveWrapperTypeMap.put(Float.class, Float.TYPE);
        primitiveWrapperTypeMap.put(Integer.class, Integer.TYPE);
        primitiveWrapperTypeMap.put(Long.class, Long.TYPE);
        primitiveWrapperTypeMap.put(Short.class, Short.TYPE);
        primitiveWrapperTypeMap.put(Void.class, Void.TYPE);
    }

    private final String pathParameter;
    private final MultiMap<String> pathParameters = new MultiMap<>();
    private final String body;
    private final String ContentType;

    public MessageUtil(String pathParameter, String body, String contentType) {
        this.pathParameter = pathParameter;
        this.body = body;
        ContentType = contentType.trim().isEmpty() ? "FORM" : contentType;
        init();
    }

    private static boolean isPrimitive(Class<?> source) {
        return source.isPrimitive() || primitiveWrapperTypeMap.containsKey(source);
    }

    private void init() {
        if (pathParameter != null) {
            UrlEncoded.decodeTo(URLDecoder.decode(pathParameter, StandardCharsets.UTF_8), pathParameters, "UTF-8");
        }
        if (body != null) {
            switch (ContentType) {
                case "JSON" -> {

                }
                case "FORM" -> {
                    UrlEncoded.decodeTo(URLDecoder.decode(body, StandardCharsets.UTF_8), pathParameters, "UTF-8");
                }
            }
        }
    }

    private Object get(String key) {
        Object o = pathParameters.get(key);
        if (o != null) {
            return o;
        }
        if (body != null && Objects.equals(ContentType, "JSON")) {
            return JSON.parseObject(body).get(key);
        }
        return null;
    }

    public Object resolve(String key, Class<?> fieldType, int parameterLength) {
        if (fieldType.isArray()) {
            //TODO 暂时不实现
        }
        if (Map.class.isAssignableFrom(fieldType)) {
            if (parameterLength == 1) {
                return Objects.isNull(body) ? pathParameters : JSON.parseObject(body, Map.class);
            } else {
                return Objects.isNull(body) ? pathParameters.get(key) : JSON.parseObject(body, Map.class).get(key);
            }
        }
        if (Collection.class.isAssignableFrom(fieldType)) {
            if (parameterLength == 1) {
                return Objects.isNull(body) ? pathParameters.get(key) : JSON.parseObject(body, Collection.class);
            } else {
                return Objects.isNull(body) ? pathParameters.get(key) : JSON.parseObject(body, Map.class).get(key);
            }
        }
        if (isPrimitive(fieldType)) {
            String o = (String) get(key);
            if (o != null) {
                if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
                    return Integer.parseInt(o);
                }
                if (Byte.class.equals(fieldType) || byte.class.equals(fieldType)) {
                    return Byte.valueOf(o);
                }
                if (Character.class.equals(fieldType) || char.class.equals(fieldType)) {
                    return o.charAt(0);
                }
                if (Double.class.equals(fieldType) || double.class.equals(fieldType)) {
                    return Double.valueOf(o);
                }
                if (Float.class.equals(fieldType) || float.class.equals(fieldType)) {
                    return Float.valueOf(o);
                }
                if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
                    return Boolean.valueOf(o);
                }
                if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
                    return Long.valueOf(o);
                }
                if (Short.class.equals(fieldType) || short.class.equals(fieldType)) {
                    return Short.valueOf(o);
                }
                return null;
            }
        } else {
            if (parameterLength == 1) {
                return JSON.parseObject(body, fieldType);
            } else {
                return JSON.parseObject(JSON.toJSONString(JSON.parseObject(body).get(key)), fieldType);
            }

        }
        return null;
    }

}
