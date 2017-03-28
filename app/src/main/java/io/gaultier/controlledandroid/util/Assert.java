package io.gaultier.controlledandroid.util;

/**
 * Created by q on 17/10/16.
 * assertions are not enabled on non rooted android devices
 * this class acts as a replacement
 */
@SuppressWarnings("ConstantConditions")
public final class Assert {

    public static void ensureNotNull(Object object) {
        if (object == null) {
            thrown("");
        }
    }

    public static void ensureNotNull(Object object, String message) {
        if (object == null) {
            thrown(message);
        }
    }


    public static void ensure(boolean flag) {
        assert flag : "";
        if (!flag) {
            thrown("");
        }
    }

    public static void ensureEquals(Object a, Object b) {
        if (!areEquals(a, b)) {
            thrown(a + " != " + b);
        }
    }

    public static boolean areEquals(Object o1, Object o2) {
        return o1 == o2 || ((o1 != null) && o1.equals(o2));
    }


    public static void ensure(boolean flag, String message) {
        assert flag : message;
        if (!flag) {
            thrown(message);
        }
    }

    public static void thrown() {
        thrown("");
    }

    public static void thrown(String message) {
        if (message.length() > 0) {
            throw new AssertionException(message);
        }
        else {
            throw new AssertionException();
        }
    }
}

