package de.offis.europeana.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is meant to be used as an interceptor. 
 * It uses the DeviceRecognition to modify the ViewName to a template 
 * that is best suited for a mobile device's capabilities. 
 * 
 * @author Dennis Heinen
 */
public class DeviceRecognitionInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private DeviceRecognition deviceRecognition;
	
	/* 
	 * modifies the ViewName to a template that is best suited for a mobile device's capabilities.
	 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Object o,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(httpServletRequest, httpServletResponse, o, modelAndView);
		if (deviceRecognition != null) {
			String currentViewName = modelAndView.getViewName();
			if (currentViewName != null) {
				modelAndView.setViewName(deviceRecognition.GetDeviceTemplate(httpServletRequest, currentViewName));
			}
		}
	}

}
