package org.context.util;

import web.http.HttpRequest;
import web.http.HttpResponse;
import web.util.MessageReader;
import web.util.TypeConverter;

import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import static java.lang.reflect.Array.set;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 5:06 下午 2021/12/3
 * @Modified By:
 */
public class MessageUtil {

    private final Map<String, MessageReader.lexec> pathParameter;
    private final byte[] body;
    private final String ContentType;

    Map<String, MessageReader.lexec> bodyLexec;

    public MessageUtil(String pathParameter, byte[] body, String contentType) {
        if (pathParameter != null) {
            this.pathParameter =
                    new MessageReader(URLDecoder.decode(pathParameter, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8)).readForm();
        } else this.pathParameter = null;
        this.body = body;
        ContentType = contentType.trim().isEmpty() ? "FORM" : contentType;
        init();
    }


    private void init() {
        if (body != null) {
            MessageReader reader = new MessageReader(body);
            switch (ContentType) {
                case "JSON" -> {
                    bodyLexec = reader.read();
                }
                case "FORM" -> {
                    this.bodyLexec = reader.readForm();
                }
            }
        }
    }

    private Object formResolve(Map<String, MessageReader.lexec> read, Parameter parameter) {
        MessageReader.lexec lexec = read.get(parameter.getName());
        if (lexec != null) {
            if (TypeConverter.isPrimitive(parameter.getType())) {
                return TypeConverter.typePrimitiveConversion(lexec, parameter.getType());
            }
            if (parameter.getType().isArray()) {
                return TypeConverter.typeArrayConversion(parameter, lexec);
            }

            if (Collection.class.isAssignableFrom(parameter.getType())) {
                return TypeConverter.typeCollectionConversion(parameter, lexec);
            }
            if (Map.class.isAssignableFrom(parameter.getType())) {
                return TypeConverter.typeMapConversion(parameter, pathParameter);
            }
            return TypeConverter.typeBeanConversion(parameter.getType(), pathParameter);
        }
        return null;
    }

    /**
     * @param parameters 方法参数类型
     * @param request    请求
     * @param response   响应
     * @return 方法参数
     */
    public Object[] resolve(Parameter[] parameters, HttpRequest request, HttpResponse response) {
        Object[] args = new Object[parameters.length];
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            if (HttpRequest.class.isAssignableFrom(parameter.getType())) {
                set(args, i, request);
                continue;
            }
            if (HttpResponse.class.isAssignableFrom(parameter.getType())) {
                set(args, i, response);
                continue;
            }
            if (pathParameter != null) {
                Object arg = formResolve(pathParameter, parameter);
                if (arg != null) {
                    set(args, i, arg);
                    continue;
                }
            }
            if (ContentType.equals("FORM")) {
                Object arg = formResolve(bodyLexec, parameter);
                if (arg != null) {
                    set(args, i, arg);
                    continue;
                }
            } else {
                MessageReader.lexec lexec = bodyLexec.getOrDefault(parameter.getName(), new MessageReader.lexec(body));
                if (TypeConverter.isPrimitive(parameter.getType())) {
                    Object arg = TypeConverter.typePrimitiveConversion(lexec, parameter.getType());
                    set(args, i, arg);
                    continue;
                }
                if (parameter.getType().isArray()) {
                    Object arg = TypeConverter.typeArrayConversion(parameter, lexec);
                    set(args, i, arg);
                    continue;
                }
                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    Object arg = TypeConverter.typeCollectionConversion(parameter, lexec);
                    set(args, i, arg);
                    continue;
                }
                if (Map.class.isAssignableFrom(parameter.getType())) {
                    Object arg = TypeConverter.typeMapConversion(parameter, new MessageReader(lexec.readAllBytes()).read());
                    set(args, i, arg);
                    continue;
                }
                Object arg = TypeConverter.typeBeanConversion(parameter, lexec);
                set(args, i, arg);
                continue;
            }
            set(args, i, null);
        }
        return args;
    }

}
