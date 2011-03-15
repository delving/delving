package eu.delving.core.util;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 11/2/10 12:47 PM
 */
public class ClickStreamLoggerInterceptor extends HandlerInterceptorAdapter {

    private static ThreadLocal<DateTime> dateTimeThreadLocal = new ThreadLocal<DateTime>();

    public static long getTimeElapsed() {
        if (dateTimeThreadLocal.get() == null) {
            Logger.getLogger(ClickStreamLoggerInterceptor.class).error("This interceptor must be installed!");
            return -1;
        }
        return System.currentTimeMillis() - dateTimeThreadLocal.get().getMillis();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        dateTimeThreadLocal.set(new DateTime());
        return true;
    }
}
