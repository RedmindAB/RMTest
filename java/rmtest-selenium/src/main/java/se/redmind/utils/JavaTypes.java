package se.redmind.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import se.redmind.rmtest.config.Configuration;

/**
 * @author Jeremy Comte
 */
public class JavaTypes {

    public static JavaType getParametizedList(Field field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        JavaType parameterType = Configuration.objectMapper().getTypeFactory().constructType(type.getActualTypeArguments()[0]);
        JavaType listType = Configuration.objectMapper().getTypeFactory().constructParametrizedType(ArrayList.class, List.class, parameterType);
        return listType;
    }

}
