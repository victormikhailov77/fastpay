package org.fastpay.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Wrapper around Method.invoke, to provide exception-safe comparator for class properties
public class MethodGetWrapper implements Comparable {

    private final Method method;
    private final Object instance;

    private MethodGetWrapper(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    public static MethodGetWrapper of(Method method, Object instance) {
        return new MethodGetWrapper(method, instance);
    }

    // invokes instanceObj.getProperty by name
    private Object invokeSelf() {
        try {
            return method.invoke(instance);
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
}
