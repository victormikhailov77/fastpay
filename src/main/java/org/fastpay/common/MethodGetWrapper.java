package org.fastpay.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Wrapper around Method.invoke, to provide exception-safe comparator for class properties
public class MethodGetWrapper<T> implements Comparable {

    private final Method method;
    private final T instance;

    private MethodGetWrapper(Method method, T instance) {
        this.method = method;
        this.instance = instance;
    }

    public static <T> MethodGetWrapper of(Method method, T instance) {
        return new MethodGetWrapper(method, instance);
    }

    // invokes instanceObj.getProperty by name
    private T invokeSelf() {
        try {
            return (T)method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new ParameterValidationException("Invalid parameter name.", ex);
        }
    }

    @Override
    public int compareTo(Object other) {
        Object arg1 = invokeSelf();
        MethodGetWrapper otherObj = (MethodGetWrapper) other;
        Object arg2 = otherObj.invokeSelf();
        // let's make it String, impossible to convert any object to Comparable
        return arg1.toString().compareTo(arg2.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Object arg1 = invokeSelf();
        MethodGetWrapper otherObj = (MethodGetWrapper) other;
        Object arg2 = otherObj.invokeSelf();
        return arg1.equals(arg2);
    }

    @Override
    public int hashCode() {
        Object arg1 = invokeSelf();
        return arg1.hashCode();
    }

}
