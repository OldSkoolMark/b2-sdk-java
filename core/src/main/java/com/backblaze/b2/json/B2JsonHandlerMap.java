/*
 * Copyright 2017, Backblaze Inc. All Rights Reserved.
 * License https://www.backblaze.com/using_b2_code.html
 */

package com.backblaze.b2.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds a mapping from Class to B2JsonTypeHandler.
 *
 * The mapping starts out with initial contents, which must be ALL
 * of the non-default mappings that will be used.  If any other
 * handlers are needed, the default B2JsonObjectHandler will be used
 * for that class.
 *
 * This class is THREAD SAFE.
 */
public class B2JsonHandlerMap {

    // access to this map is always synchronized on this object.
    // we think it's safe to overwrite the entry for a given class because
    // we assume all handlers are stateless and any two instances of
    // a handler for a class are equivalent.
    private final Map<Class<?>, B2JsonTypeHandler<?>> map = new HashMap<>();

    public B2JsonHandlerMap() {
        this(null);
    }
    /**
     * Sets up a new map.
     */
    private B2JsonHandlerMap(Map<Class<?>, B2JsonTypeHandler<?>> initialMapOrNull) {
        // add all built-in handlers.
        map.put(BigDecimal.class, new B2JsonBigDecimalHandler());
        map.put(BigInteger.class, new B2JsonBigIntegerHandler());
        map.put(boolean.class, new B2JsonBooleanHandler(true));
        map.put(Boolean.class, new B2JsonBooleanHandler(false));
        map.put(byte.class, new B2JsonByteHandler(true));
        map.put(Byte.class, new B2JsonByteHandler(false));
        map.put(char.class, new B2JsonCharacterHandler(true));
        map.put(Character.class, new B2JsonCharacterHandler(false));
        map.put(int.class, new B2JsonIntegerHandler(true));
        map.put(Integer.class, new B2JsonIntegerHandler(false));
        map.put(LocalDate.class, new B2JsonLocalDateHandler());
        map.put(LocalDateTime.class, new B2JsonLocalDateTimeHandler());
        map.put(Duration.class, new B2JsonDurationHandler());
        map.put(long.class, new B2JsonLongHandler(true));
        map.put(Long.class, new B2JsonLongHandler(false));
        map.put(float.class, new B2JsonFloatHandler(true));
        map.put(Float.class, new B2JsonFloatHandler(false));
        map.put(double.class, new B2JsonDoubleHandler(true));
        map.put(Double.class, new B2JsonDoubleHandler(false));
        map.put(String.class, new B2JsonStringHandler());
        map.put(boolean[].class, new B2JsonBooleanArrayHandler(map.get(boolean.class)));
        map.put(char[].class, new B2JsonCharArrayHandler(map.get(char.class)));
        map.put(byte[].class, new B2JsonByteArrayHandler(map.get(byte.class)));
        map.put(int[].class, new B2JsonIntArrayHandler(map.get(int.class)));
        map.put(long[].class, new B2JsonLongArrayHandler(map.get(long.class)));
        map.put(float[].class, new B2JsonFloatArrayHandler(map.get(float.class)));
        map.put(double[].class, new B2JsonDoubleArrayHandler(map.get(double.class)));

        if (initialMapOrNull != null) {
            map.putAll(initialMapOrNull);
        }
    }

    /**
     * Gets the handler for a given class at the top level.
     */
    public synchronized <T> B2JsonTypeHandler<T> getHandler(Class<T> clazz) throws B2JsonException {

        @SuppressWarnings("unchecked")
        B2JsonTypeHandler<T> result = lookupHandler(clazz);

        if (result == null) {
            // maybe use a custom handler provided by clazz.
            result = findCustomHandler(clazz);
            if (result != null) {
                rememberHandler(clazz, result);
            }
        }

        if (result == null) {
            if (clazz.isEnum()) {
                result = new B2JsonEnumHandler<>(clazz);
                rememberHandler(clazz, result);
            } else if (clazz.isArray()) {
                final Class eltClazz = clazz.getComponentType();
                B2JsonTypeHandler eltClazzHandler = getHandler(eltClazz);
                //noinspection unchecked
                result = (B2JsonTypeHandler<T>) new B2JsonObjectArrayHandler(clazz, eltClazz, eltClazzHandler);
                rememberHandler(clazz, result);
            } else if (isUnionBase(clazz)) {
                result = (B2JsonTypeHandler<T>) new B2JsonUnionBaseHandler(clazz, this);
                rememberHandler(clazz, result);
            } else {
                //noinspection unchecked
                result = (B2JsonTypeHandler<T>) new B2JsonObjectHandler(clazz, this);
                rememberHandler(clazz, result);
            }
        }

        return result;
    }

    /**
     * Is this class the base class for a union type?
     *
     * Union bases have the @union annotation.
     */
    /*package*/ static <T> boolean isUnionBase(Class<T> clazz) {
        return clazz.getAnnotation(B2Json.union.class) != null;
    }

    private <T> B2JsonTypeHandler<T> findCustomHandler(Class<T> clazz) throws B2JsonException {
        // this does NOT need to be synchronized because it doesn't touch the map.

        // i'm using getDeclaredMethod instead of just getMethod so that classes
        // can't inherit the type handler from their superclass.  that seems like
        // a safer starting point.
        Method method = null;
        try {
            method = clazz.getDeclaredMethod("getJsonTypeHandler");
            method.setAccessible(true);
            final Object obj = method.invoke(null);
            if (obj instanceof B2JsonTypeHandler) {
                //noinspection unchecked
                return (B2JsonTypeHandler<T>) obj;
            } else {
                String objType = (obj == null) ? "null" : obj.getClass().getName();
                throw new B2JsonException(clazz.getSimpleName() + "." + method.getName() + "() returned an unexpected type of object (" + objType + ")");
            }
        } catch (NoSuchMethodException e) {
            // this class just didn't declare a handler.  oh well.
            return null;
        } catch (InvocationTargetException e) {
            throw new B2JsonException("failed to invoke " + method + ": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new B2JsonException("illegal access to " + method + ": " + e.getMessage(), e);
        }
    }

    private synchronized <T> B2JsonTypeHandler<T> lookupHandler(Class<T> clazz) {
        // this is a method to make it easy to synchronize it.  it's private, so
        // i'm hoping the compiler considers inlining it.
        //noinspection unchecked
        return (B2JsonTypeHandler<T>) map.get(clazz);
    }

    /**
     * Saves a handler in the map, remembering to use it for the given class.
     *
     * This method is not private because it is needed by B2JsonObjectHandler,
     * so that it can store itself in the map before trying to make handlers
     * for its fields, which may be recursive and be of the same type.  When
     * this happens, the handler stored IS NOT READY YET, because its constructor
     * is not done yet.  This is safe because it all happens within a call
     * to B2JsonHandlerMap.getHandler(), which synchronized and keeps anybody
     * else from seeing the B2JsonObjectHandler before it is fully constructed.
     */
    protected synchronized <T> void rememberHandler(Class<T> clazz, B2JsonTypeHandler<T> handler) {
        map.put(clazz, handler);
    }
}
