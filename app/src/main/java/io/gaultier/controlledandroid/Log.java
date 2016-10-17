package io.gaultier.controlledandroid;

/**
 * Created by q on 17/10/16.
 *
 * using this indirection we are able to strip string building and concatenation from the release version
 * @see http://stackoverflow.com/questions/7086920/removing-logging-with-proguard-doesnt-remove-the-strings-being-logged
 */
public final class Log {

    private static final int LOG_MAX_LENGTH = 4000;
    private static final boolean enableLogs = true;

    public static void v(String TAG, Object a) {
        innerV(TAG, a);
    }

    public static void v(String TAG, Object a, Object b) {
        innerV(TAG, a, b);
    }

    public static void v(String TAG, Object a, Object b, Object c) {
        innerV(TAG, a, b, c);
    }

    public static void v(String TAG, Object a, Object b, Object c, Object d) {
        innerV(TAG, a, b, c, d);
    }

    public static void v(String TAG, Object a, Object b, Object c, Object d, Object e) {
        innerV(TAG, a, b, c, d, e);
    }

    public static void v(String TAG, Object a, Object b, Object c, Object d, Object e, Object f) {
        innerV(TAG, a, b, c, d, e, f);
    }

    public static void v(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g) {
        innerV(TAG, a, b, c, d, e, f, g);
    }

    public static void longV(String TAG, String a) {
        innerLongV(TAG, a);
    }

    public static void d(String TAG, Object a) {
        innerD(TAG, a);
    }

    public static void d(String TAG, Object a, Object b) {
        innerD(TAG, a, b);
    }

    public static void d(String TAG, Object a, Object b, Object c) {
        innerD(TAG, a, b, c);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d) {
        innerD(TAG, a, b, c, d);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e) {
        innerD(TAG, a, b, c, d, e);
    }
    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f) {
        innerD(TAG, a, b, c, d, e, f);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g) {
        innerD(TAG, a, b, c, d, e, f, g);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h) {
        innerD(TAG, a, b, c, d, e, f, g, h);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {
        innerD(TAG, a, b, c, d, e, f, g, h, i);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j) {
        innerD(TAG, a, b, c, d, e, f, g, h, i, j);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k) {
        innerD(TAG, a, b, c, d, e, f, g, h, i, j, k);
    }

    public static void d(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, Object l ) {
        innerD(TAG, a, b, c, d, e, f, g, h, i, j, k, l);
    }

    public static void d(String TAG, Throwable throwable) {
        innerD(TAG, throwable);
    }

    public static void i(String TAG, Object a) {
        innerI(TAG, a);
    }

    public static void i(String TAG, Object a, Object b) {
        innerI(TAG, a, b);
    }

    public static void i(String TAG, Object a, Object b, Object c) {
        innerI(TAG, a, b, c);
    }

    public static void i(String TAG, Object a, Object b, Object c, Object d) {
        innerI(TAG, a, b, c, d);
    }

    public static void i(String TAG, Object a, Object b, Object c, Object d, Object e) {
        innerI(TAG, a, b, c, d, e);
    }

    public static void i(String TAG, Object a, Object b, Object c, Object d, Object e, Object f) {
        innerI(TAG, a, b, c, d, e, f);
    }

    public static void i(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g) {
        innerI(TAG, a, b, c, d, e, f, g);
    }

    public static void i(String TAG, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h) {
        innerI(TAG, a, b, c, d, e, f, g, h);
    }

    public static void w(String TAG, Object a) {
        innerW(TAG, a);
    }

    public static void w(String TAG, Object a, Object b) {
        innerW(TAG, a, b);
    }

    public static void w(String TAG, Object a, Object b, Object c) {
        innerW(TAG, a, b, c);
    }

    public static void w(String TAG, Object a, Object b, Object c, Object d) {
        innerW(TAG, a, b, c, d);
    }

    public static void w(String TAG, Object a, Object b, Object c, Object d, Object e) {
        innerW(TAG, a, b, c, d, e);
    }

    public static void w(String TAG, Object a, Object b, Object c, Object d, Object e, Object f) {
        innerW(TAG, a, b, c, d, e, f);
    }

    public static void e(String TAG, Object a) {
        innerE(TAG, a);
    }

    public static void e(String TAG, Object a, Object b) {
        innerE(TAG, a, b);
    }

    public static void e(String TAG, Object a, Object b, Object c) {
        innerE(TAG, a, b, c);
    }

    public static void e(String TAG, Object a, Object b, Object c, Object d) {
        innerE(TAG, a, b, c, d);
    }

    public static void e(String TAG, Object a, Object b, Object c, Object d, Object e) {
        innerE(TAG, a, b, c, d, e);
    }

    public static void e(String TAG, Throwable throwable, Object a) {
        innerE(TAG, throwable, a);
    }

    public static void e(String TAG, Throwable throwable, Object a, Object b) {
        innerE(TAG, throwable, a, b);
    }

    private static void innerV(String TAG, Object... msgs) {
        if (enableLogs) {
            android.util.Log.v(TAG, buildLogMessage(msgs));
        }
    }

    private static void innerLongV(String TAG, String msg) {
        if (enableLogs) {
            int length = msg.length();
            int trunkCount = length / LOG_MAX_LENGTH;
            for (int i = 0; i <= trunkCount; ++i) {
                android.util.Log.v(TAG, buildLogMessage(msg.substring(LOG_MAX_LENGTH * i, Math.min(LOG_MAX_LENGTH * (i + 1), length))));
            }
        }
    }

    private static void innerD(String TAG, Object... msgs) {
        if (enableLogs) {
            android.util.Log.d(TAG, buildLogMessage(msgs));
        }
    }

    private static void innerD(String TAG, Throwable throwable, Object... msgs) {
        if (enableLogs) {
            android.util.Log.d(TAG, buildLogMessage(msgs), throwable);
        }
    }

    private static void innerI(String TAG, Object... msgs) {
        if (enableLogs) {
            android.util.Log.i(TAG, buildLogMessage(msgs));
        }
    }

    private static void innerW(String TAG, Object... msgs) {
        if (enableLogs) {
            android.util.Log.w(TAG, buildLogMessage(msgs));
        }
    }

    private static void innerE(String TAG, Object... msgs) {
        if (enableLogs) {
            android.util.Log.e(TAG, buildLogMessage(msgs));
        }
    }

    private static void innerE(String TAG, Throwable throwable, Object... msgs) {
        if (enableLogs) {
            android.util.Log.e(TAG, buildLogMessage(msgs), throwable);
        }
    }

    public static String buildLogMessage(Object... msgs) {
        StringBuilder logMessage = new StringBuilder();
        for(Object msg : msgs) {
            if (msg == null) {
                logMessage.append("null");
            } else {
                logMessage.append(msg.toString());
            }
            logMessage.append(" ");
        }
        return logMessage.toString();
    }
}
