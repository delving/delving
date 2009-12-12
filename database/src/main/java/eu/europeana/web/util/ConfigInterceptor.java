package eu.europeana.web.util;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author vitali  Kiruta
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ConfigInterceptor extends HandlerInterceptorAdapter {

    private Map config;

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        super.postHandle(httpServletRequest, httpServletResponse, o, modelAndView);

        // adding the responsetype set in the ModelAndView to the response
        httpServletResponse.setContentType(modelAndView.getModel().get("contentType").toString());

        if (Boolean.valueOf((String) config.get("piwik.enabled"))) {
            modelAndView.addObject("piwik_js", config.get("piwik.jsUrl"));
            modelAndView.addObject("piwik_log_url", config.get("piwik.logUrl"));
        }
        if (!modelAndView.getViewName().startsWith("redirect:")) {
            modelAndView.addObject("debug", Boolean.valueOf((String) config.get("debug")));
            modelAndView.addObject("cacheUrl", config.get("cacheUrl"));
            modelAndView.addObject("staticPagesSource", config.get("message.static_pages"));
        }
    }

    public void setConfig(Map config) {
        this.config = config;
    }

}
