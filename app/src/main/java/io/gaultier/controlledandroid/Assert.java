package io.gaultier.controlledandroid;

/**
 * Created by q on 17/10/16.
 * assertions are not enabled on non rooted android devices
 * this class acts as a replacement
 */
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
        if (flag == false) {
            thrown("");
        }
    }

    public static void ensure(boolean flag, String message) {
        if (flag == false) {
            thrown(message);
        }
    }

    public static void thrown() {
        thrown(new String());
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

