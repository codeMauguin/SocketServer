package org.context.util;

import com.alibaba.fastjson.JSON;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;
import web.http.HttpRequest;
import web.http.HttpResponse;
import web.util.ConfigReader;
import web.util.MessageReader;
import web.util.TypeConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
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
        primitiveWrapperTypeMap.put(String.class, String.class);
    }

    private final String pathParameter;
    private final MultiMap<String> pathParameters = new MultiMap<>();
    private final byte[] body;
    private final String ContentType;

    Map<String, MessageReader.lexec> bodyLexec;

    public MessageUtil(String pathParameter, byte[] body, String contentType) {
        this.pathParameter = pathParameter;
        this.body = body;
        ContentType = contentType.trim().isEmpty() ? "FORM" : contentType;
        init();
    }

    public static boolean isPrimitive(Class<?> source) {
        return source.isPrimitive() || primitiveWrapperTypeMap.containsKey(source);
    }

    private void init() {
        if (pathParameter != null && !pathParameter.equals("")) {
            UrlEncoded.decodeTo(URLDecoder.decode(pathParameter, StandardCharsets.UTF_8), pathParameters, "UTF-8");
        }
        if (body != null) {
            switch (ContentType) {
                case "JSON" -> {
                    MessageReader reader = new MessageReader(body);

                    bodyLexec = reader.read();
                }
                case "FORM" -> {
                    UrlEncoded.decodeTo(URLDecoder.decode(new String(body, StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                            , pathParameters,
                            "UTF-8");
                }
            }
        }
    }

    public Object[] resolve(Parameter[] parameters, HttpRequest request, HttpResponse response) {
        Object[] args = new Object[parameters.length];
        //只从body读数据
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            if (HttpRequest.class.isAssignableFrom(parameter.getType())) {
                Array.set(args, i, request);
                continue;
            }
            if (HttpResponse.class.isAssignableFrom(parameter.getType())) {
                Array.set(args, i, response);
                continue;
            }
            MessageReader.lexec lexec = getLexec(parameter.getName());
            if (lexec != null) {
                if (TypeConverter.isPrimitive(parameter.getType())) {
                    Object arg = TypeConverter.typePrimitiveConversion(lexec, parameter.getType());
                    Array.set(args, i, arg);
                    continue;
                }
                if (parameter.getType().isArray()) {
                    Object arg = TypeConverter.typeArrayConversion(parameter, lexec);
                    Array.set(args, i, arg);
                    continue;
                }
                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    Object arg = TypeConverter.typeCollectionConversion(parameter, lexec);
                    Array.set(args, i, arg);
                    continue;
                }
                if (Map.class.isAssignableFrom(parameter.getType())) {
                    Object arg = TypeConverter.typeMapConversion(parameter, lexec);
                    Array.set(args, i, arg);
                    continue;
                }
                Object arg = TypeConverter.typeBeanConversion(parameter, lexec);
                Array.set(args, i, arg);
            } else {
                if (TypeConverter.isPrimitive(parameter.getType())) {
                    Object arg = TypeConverter.typePrimitiveConversion(new MessageReader.lexec(body), parameter.getType());
                    Array.set(args, i, arg);
                    continue;
                }
                if (Map.class.isAssignableFrom(parameter.getType())) {
                    Object arg = TypeConverter.typeMapConversion(parameter, new MessageReader.lexec(body));
                    Array.set(args, i, arg);
                    continue;
                }
                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    Object arg = TypeConverter.typeCollectionConversion(parameter, new MessageReader.lexec(body));
                    Array.set(args, i, arg);
                    continue;
                }
                Object arg = TypeConverter.typeBeanConversion(parameter, new MessageReader.lexec(body));
                Array.set(args, i, arg);
            }
        }
        return args;
    }


    public <T> Object resolve(String key, Class<T> fieldType, int parameterLength) {
        if (Objects.equals(ContentType, "JSON")) {
            ConfigReader reader = new ConfigReader(body);
            if (parameterLength == 1) {
                if (fieldType.isArray()) {
                    String[] res = reader.readArray();
                    return getArray(fieldType, res);
                }
                if (Map.class.isAssignableFrom(fieldType)) {
                    return reader.readMap();
                }
                if (Collection.class.isAssignableFrom(fieldType)) {
                }
            }
            if (fieldType.isArray()) {
                try {
                    String[] res = reader.readArray();
                    return getArray(fieldType, res);
                } catch (Exception ignore) {
                    reader.reset();
                    return getArray(fieldType, (String[]) reader.readMap().get(key));
                }
            }
            if (Collection.class.isAssignableFrom(fieldType)) {
                return JSON.parseObject(body, fieldType);
            }
            Map result = new ConfigReader(body).readMap();
            if (Map.class.isAssignableFrom(fieldType)) {
                return result;
            } else if (result.get(key) == null) ;
            else if (primitiveWrapperTypeMap.containsKey(fieldType) || fieldType.isPrimitive()) {
                return TypeConverter.primitiveConversion((String) result.get(key), fieldType);
            } else {
                return JSON.parseObject(body, fieldType);
            }
        } else {
            if (fieldType.isAssignableFrom(pathParameters.getString(key).getClass()))
                return pathParameters.getString(key);
            if (Map.class.isAssignableFrom(fieldType)) {
                return pathParameters;
            } else if (
                    primitiveWrapperTypeMap.containsKey(fieldType) || fieldType.isPrimitive()
            ) {
                return TypeConverter.typeConversion(pathParameters.getString(key), fieldType);
            }
        }
        return null;
    }

    private <T> T getArray(Class<T> fieldType, String[] res) {
        Object array = Array.newInstance(fieldType.componentType(), res.length);
        for (int i = 0; i < res.length; i++) {
            String re = res[i];
            Array.set(array, i, TypeConverter.typeConversion(re, fieldType.componentType()));
        }
        return (T) array;
    }

    private MessageReader.lexec getLexec(String key) {
        return bodyLexec.get(key);
    }
}
