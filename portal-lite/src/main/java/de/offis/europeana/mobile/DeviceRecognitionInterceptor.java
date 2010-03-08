/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

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
 * @author Dennis Heinen <Dennis.Heinen@offis.de>
 */
public class DeviceRecognitionInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private DeviceRecognition deviceRecognition;

	/*
	 * modifies the ViewName to a template that is best suited for a mobile device's capabilities.
	 * Information about the device's screen width & height is added to ModelAndView
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
                DeviceRecognitionResult deviceRecognitionResult = deviceRecognition.getDeviceInformation(httpServletRequest, currentViewName);
                if (deviceRecognitionResult != null) {
                    modelAndView.setViewName(deviceRecognitionResult.GetTemplateName());
                    modelAndView.addObject("device_screen_width", deviceRecognitionResult.GetDeviceScreenWidth());
                    modelAndView.addObject("device_screen_height", deviceRecognitionResult.GetDeviceScreenHeight());
                }
			}
		}
	}

}
