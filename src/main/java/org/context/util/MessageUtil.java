package org.context.util;

import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;
import web.http.HttpRequest;
import web.http.HttpResponse;
import web.util.MessageReader;
import web.util.TypeConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

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

    private MessageReader.lexec getLexec(String key) {
        return bodyLexec.get(key);
    }
}
