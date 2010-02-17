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

import net.sourceforge.wurfl.core.Device;
import net.sourceforge.wurfl.core.WURFLHolder;
import net.sourceforge.wurfl.core.WURFLManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * This class encapsulates the WURFL-Browser detection
 *  
 * 
 * @author Dennis Heinen <Dennis.Heinen@offis.de>
 *
 */
public class DeviceRecognition {

	private static final Logger log = Logger.getLogger(DeviceRecognition.class);

	private WURFLManager _wurfl = null;
	private ServletContext servletContext = null;
	
	/**
	 * Returns the ServletContext
	 * 
	 * @return the ServletContext
	 */
	public ServletContext getContext() {
		return servletContext;
	}
	
	/**
	 * Sets the ServletContext, used to create the WurflManager
	 * @param servletContext
	 */
	public void setContext(ServletContext servletContext) {
		this.servletContext = servletContext;
		if (this.servletContext != null) {
			WURFLHolder wurflHolder = (WURFLHolder) this.servletContext.getAttribute("net.sourceforge.wurfl.core.WURFLHolder");
			if (wurflHolder != null) {
				_wurfl = wurflHolder.getWURFLManager();
			}
		}
	}

	/**
	 * Returns a template for the current device, best suited for its capabilities. 
	 * e.g. if template is 'index_orig' and the device's screen is 320x480, 
	 * the returned String is 'mobile\320x480\index_orig' > the template is chosen 
	 * from the mobile\320x480 subdirectory of the template path
	 * This method will evolve during the course of the project.
	 * 
	 * @param request The request, used to determine the device
	 * @param template The template that shall be used
	 * @return Best suited template for mobile device
	 */
	public String GetDeviceTemplate(HttpServletRequest request, String template) {
		String result = template;
		if (template != null) {
				if (_wurfl != null) {
					Device device = _wurfl.getDeviceForRequest(request);
					if (device != null && "true".equals(device.getCapability("is_wireless_device"))) { //desktop browsers will see the desired page, this is for mobile only
						//The iphone templates are already available:
						if (device.getUserAgent().toLowerCase().indexOf("safari") > 0){
							result = "mobile/iphone/"+template;
						} else {
							//TODO: this redirects any mobile browser to a placeholder page, needs to be changed as soon as the appropriate templates are available
							result = "mobile/320x480/index_orig";
						}
						log.info("template and subfolder after mobile device recognition: " + result);

					}
				}
		}
		return result;
	}

}
