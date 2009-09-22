package eu.europeana.dashboard.server;

/**
 * Hold a cookie in thread local
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UserCookie {
    private static ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<String>();

    public static void set(String cookie) {
        THREAD_LOCAL.set(cookie);
    }

    public static String get() {
        return THREAD_LOCAL.get();
    }
}
