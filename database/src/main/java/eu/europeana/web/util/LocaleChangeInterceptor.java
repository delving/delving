package eu.europeana.web.util;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;


public class LocaleChangeInterceptor extends HandlerInterceptorAdapter {

    public static final String DEFAULT_PARAM_NAME = "locale";

    private String paramName = DEFAULT_PARAM_NAME;


    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return this.paramName;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {

        System.out.println("Detected Language Change!"); // todo: replace with inject RequestLogger
        String newLocale = request.getParameter(this.paramName);
        if (newLocale != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver == null) {
                throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
            }
            LocaleEditor localeEditor = new LocaleEditor();
            localeEditor.setAsText(newLocale);
            localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
        }
        // Proceed in any case.
        return true;
    }

}

