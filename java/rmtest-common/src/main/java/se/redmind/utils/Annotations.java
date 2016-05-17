package se.redmind.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * @author Jeremy Comte
 */
@SuppressWarnings("unchecked")
public class Annotations {

    public static <A extends Annotation> A defaultOf(Class<A> annotation) {
        return (A) Proxy.newProxyInstance(annotation.getClassLoader(), new Class<?>[]{annotation}, (proxy, method, args) -> {
            return method.getDefaultValue();
        });
    }

    public static <A extends Annotation> A combine(List<A> annotations) {
        Preconditions.checkArgument(!annotations.isEmpty(), "annotations cannot be an empty");
        Class<A> clazz = (Class<A>) annotations.get(0).annotationType();
        return (A) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, (proxy, method, args) -> {
            if (method.getName().equals("toString")) {
                StringBuilder stringBuilder = new StringBuilder("@").append(clazz.getCanonicalName()).append("(");
                for (Method properties : clazz.getDeclaredMethods()) {
                    Object value = getOverridingOrDefaultValue(properties, annotations);
                    stringBuilder.append(properties.getName()).append("=");
                    if (value.getClass().isArray()) {
                        stringBuilder.append(Arrays.toString((Object[]) value));
                    } else {
                        stringBuilder.append(value);
                    }
                    stringBuilder.append(", ");
                }
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append(")");
                return stringBuilder.toString();
            } else {
                return getOverridingOrDefaultValue(method, annotations);
            }
        });
    }

    private static <A extends Annotation> Object getOverridingOrDefaultValue(Method method, List<A> annotations) throws IllegalArgumentException, InvocationTargetException, IllegalAccessException {
        Object defaultValue = method.getDefaultValue();
        for (A annotation : annotations) {
            Object currentValue = method.invoke(annotation);
            if ((defaultValue.getClass().isArray() && !Arrays.toString((Object[]) defaultValue).equals(Arrays.toString((Object[]) currentValue)))
                || (!defaultValue.getClass().isArray() && !currentValue.equals(defaultValue))) {
                return currentValue;
            }
        }
        return method.getDefaultValue();
    }

    public static <A extends Annotation> A collectAndCombine(Class<A> annotationClass, Class<?> clazz) {
        List<A> annotations = collect(annotationClass, clazz);
        if (annotations.isEmpty()) {
            return defaultOf(annotationClass);
        }
        return combine(annotations);
    }

    public static <A extends Annotation> List<A> collect(Class<A> annotationClass, Class<?> clazz) {
        return collect(annotationClass, clazz, new ArrayList<>());
    }

    private static <A extends Annotation> List<A> collect(Class<A> annotationClass, Class<?> clazz, List<A> annotations) {
        if (clazz.isAnnotationPresent(annotationClass)) {
            annotations.add(clazz.getAnnotation(annotationClass));
        }
        if (clazz.getSuperclass() != null) {
            collect(annotationClass, clazz.getSuperclass(), annotations);
        }
        return annotations;
    }
}
