package se.redmind.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeremy Comte
 */
public final class Methods {

    private static final Logger LOGGER = LoggerFactory.getLogger(Methods.class);
    private static final Map<String, Method> METHOD_CACHE = new LinkedHashMap<>();

    private Methods() {
    }

    public static Object invoke(Object instance, String name, Object... parameters) {
        try {
            Class<?>[] parameterTypes = new Class<?>[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                parameterTypes[i] = parameters[i].getClass();
            }
            return getMethod(instance.getClass(), name, parameterTypes).invoke(instance, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            UncheckedThrow.throwUnchecked(ex.getCause());
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        String methodSignature = getMethodSignature(clazz, name, parameterTypes);
        Method method = METHOD_CACHE.get(methodSignature);
        if (method == null) {
            method = findMethod(clazz, name, parameterTypes);
            if (method == null) {
                throw new NoSuchMethodException(methodSignature + " doesn't exist on " + clazz.getName());
            }
            METHOD_CACHE.put(methodSignature, method);
        }
        return method;
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = null;
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(name)) {
                boolean isApplicableMethod = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!declaredMethod.getParameterTypes()[i].isAssignableFrom(parameterTypes[i])) {
                        isApplicableMethod = false;
                        break;
                    }
                }
                if (isApplicableMethod) {
                    method = declaredMethod;
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                }
            }
        }
        if (method == null && clazz.getSuperclass() != null) {
            method = findMethod(clazz.getSuperclass(), name, parameterTypes);
        }
        return method;
    }

    private static String getMethodSignature(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        return clazz.getCanonicalName() + "." + name + Arrays.toString(parameterTypes);
    }

}
