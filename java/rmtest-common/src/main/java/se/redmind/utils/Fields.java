package se.redmind.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Jeremy Comte
 */
public final class Fields {

    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new LinkedHashMap<>();

    private Fields() {
    }

    public static Table<String, Object, Field> listByPathAndDeclaringInstance(Object instance) {
        return listByPathAndDeclaringInstance(instance, field -> true);
    }

    public static Table<String, Object, Field> listByPathAndDeclaringInstance(Object instance, Predicate<Field> filter) {
        filter = filter.and(field -> !Modifier.isStatic(field.getModifiers()));
        Table<String, Object, Field> fieldsByPathAndDeclaringInstance = HashBasedTable.create();
        try {
            listByPathAndInstance(instance, filter, fieldsByPathAndDeclaringInstance, "");
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LoggerFactory.getLogger(Fields.class).error(ex.getMessage(), ex);
        }
        return fieldsByPathAndDeclaringInstance;
    }

    private static void listByPathAndInstance(Object instance, Predicate<Field> filter, Table<String, Object, Field> fieldsByPathAndDeclaringInstance, String currentPath)
        throws IllegalArgumentException, IllegalAccessException {
        for (Field field : getFieldsByNameOf(instance.getClass()).values()) {
            if (filter.test(field)) {
                fieldsByPathAndDeclaringInstance.put(currentPath + field.getName(), instance, field);
                Object value = field.get(instance);
                if (value != null) {
                    Class<?> wrappedType = ClassUtils.primitiveToWrapper(field.getType());
                    if (!wrappedType.getCanonicalName().startsWith("java")) {
                        listByPathAndInstance(field.get(instance), filter, fieldsByPathAndDeclaringInstance, currentPath + field.getName() + ".");
                    }
                }
            }
        }
    }

    public static void set(Object instance, String fieldName, Object value) {
        try {
            Fields.getField(instance.getClass(), fieldName).set(instance, value);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            LoggerFactory.getLogger(Fields.class).error(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> E getValue(Object instance, String fieldName) {
        try {
            return (E) getField(instance.getClass(), fieldName).get(instance);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field field = getFieldsByNameOf(clazz).get(name);
        if (field == null) {
            throw new NoSuchFieldException(name + " doesn't exist on " + clazz.getName());
        }
        return field;
    }

    public static Map<String, Field> getFieldsByNameOf(Class<?> clazz) {
        Map<String, Field> fieldCache = FIELD_CACHE.get(clazz);
        if (fieldCache == null) {
            fieldCache = new LinkedHashMap<>();
            recursivelyCacheFieldsOf(clazz, fieldCache);
            FIELD_CACHE.put(clazz, fieldCache);
        }
        return fieldCache;
    }

    private static void recursivelyCacheFieldsOf(Class<?> clazz, Map<String, Field> fields) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!fields.containsKey(field.getName())) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                fields.put(field.getName(), field);
            }
        }
        if (clazz.getSuperclass() != null) {
            recursivelyCacheFieldsOf(clazz.getSuperclass(), fields);
        }
    }
}
