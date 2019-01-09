package org.fastpay.common;

import lombok.Data;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MethodGetWrapperTest {

    @Data
    private class ForTest {
        private String internal;
        ForTest(String str) {
            this.internal = str;
        }
    }

    @Test
    public void shouldWrappersBeEqual() throws NoSuchMethodException {
        ForTest s1 = new ForTest("abc");
        ForTest s2 = new ForTest("abc");
        assertEquals(s1, s2);

        Method method = ForTest.class.getMethod("getInternal");
        MethodGetWrapper<ForTest> m1 = MethodGetWrapper.of(method, s1);
        MethodGetWrapper<ForTest> m2 = MethodGetWrapper.of(method, s2);
        assertEquals(m1, m2);
    }

    @Test
    public void shouldWrapperHashCodesBeEqual() throws NoSuchMethodException {
        Method method = ForTest.class.getMethod("getInternal");
        MethodGetWrapper<ForTest> m1 = MethodGetWrapper.of(method, new ForTest("abc"));
        MethodGetWrapper<ForTest> m2 = MethodGetWrapper.of(method, new ForTest("abc"));
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void shouldWrappersBeComparedLess() throws NoSuchMethodException {
        ForTest s1 = new ForTest("abc");
        ForTest s2 = new ForTest("def");
        assertTrue(s1.getInternal().compareTo(s2.getInternal()) < 0);

        Method method = ForTest.class.getMethod("getInternal");
        MethodGetWrapper<ForTest> m1 = MethodGetWrapper.of(method, s1);
        MethodGetWrapper<ForTest> m2 = MethodGetWrapper.of(method, s2);
        assertTrue(m1.compareTo(m2) < 0);
    }

    @Test
    public void shouldWrappersBeComparedGreater() throws NoSuchMethodException {
        ForTest s1 = new ForTest("ddd");
        ForTest s2 = new ForTest("aaa");
        assertTrue(s1.getInternal().compareTo(s2.getInternal()) > 0);

        Method method = ForTest.class.getMethod("getInternal");
        MethodGetWrapper<ForTest> m1 = MethodGetWrapper.of(method, s1);
        MethodGetWrapper<ForTest> m2 = MethodGetWrapper.of(method, s2);
        assertTrue(m1.compareTo(m2) > 0);
    }

    @Test
    public void shouldWrappersBeComparedEqual() throws NoSuchMethodException {
        ForTest s1 = new ForTest("aaa");
        ForTest s2 = new ForTest("aaa");
        Method method = ForTest.class.getMethod("getInternal");
        MethodGetWrapper<ForTest> m1 = MethodGetWrapper.of(method, s1);
        MethodGetWrapper<ForTest> m2 = MethodGetWrapper.of(method, s2);
        assertTrue(m1.compareTo(m2) == 0);
    }
}
