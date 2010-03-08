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

	private boolean _simpleDesktopDetectionEnabled = true;
	private WURFLManager _wurfl = null;
	private ServletContext servletContext = null;
	private Device _genericWebBrowser = null;
	
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
				_genericWebBrowser = wurflHolder.getWURFLUtils().getDeviceById("generic_web_browser");
			}
		}
	}

	/**
	 * Returns an object that contains a template name
     * for the current device, best suited for its capabilities.
	 * e.g. if template is 'index_orig' and the device's screen is 320x480, 
	 * the returned String is 'mobile\320x480\index_orig' > the template is chosen 
	 * from the mobile\320x480 subdirectory of the template path
	 * This method will evolve during the course of the project.
	 * 
	 * @param request The request, used to determine the device
	 * @param template The template that shall be used
	 * @return DeviceRecognitionResult that contains the screen width & height and the appropriate template name
	 */
	public DeviceRecognitionResult getDeviceInformation(HttpServletRequest request, String template) {
		String result = template;
		if (template != null) {
				if (_wurfl != null) {
					String userAgent = request.getHeader("User-Agent");
					log.info("UserAgent: "+userAgent);
					Device device;
					if (_simpleDesktopDetectionEnabled && 
							SimpleDesktopUserAgentMatcher.isDesktopBrowser(userAgent)) {
						device = _genericWebBrowser;
					} else {
						device = _wurfl.getDeviceForRequest(userAgent);
					}
					if (device != null && "true".equals(device.getCapability("is_wireless_device"))) { //desktop browsers will see the desired page, this is for mobile only
                        String tmp = device.getCapability("resolution_width");
                        int height, width;
                        width = Integer.parseInt(tmp);
                        tmp = device.getCapability("resolution_height");
                        height = Integer.parseInt(tmp);

                        String model_name =  device.getCapability("model_name");
                        String mobile_browser = device.getCapability("mobile_browser");
                        String device_os = device.getCapability("device_os");
                        String pointing_method = device.getCapability("pointing_method");

                        Boolean show_iphone_version = false;

                        //Identify devices that are capable to display the iphone templates
						if ("touchscreen".equalsIgnoreCase(pointing_method)){
                            if ("Safari".equalsIgnoreCase(mobile_browser) &&
                                    ("iPhone".equals(model_name) || "iPod Touch".equals(model_name))) { //maybe the iPad can be added here in the future
                                show_iphone_version = true;
                            } else if ("Android".equalsIgnoreCase(device_os) && "Android Webkit".equalsIgnoreCase(mobile_browser)) {
                                show_iphone_version = true;
                            }
                        }
                        if (show_iphone_version) {
                            result = "mobile/iphone/"+template;
						} else {
							log.info("width: "+width+" height: "+height+" device: "+device.getId());
                            //devices with a screen larger than 800x600 should be able to handle the desktop version of the portal
                            if ((width <600 && height < 800) || (width == 600 && height < 800 || (width <600 && height == 800))) {
                                   result = "mobile/generic/"+template;
                            }
						}
                        log.info("template and subfolder after mobile device recognition: " + result);
                        return new DeviceRecognitionResult(result, width, height);
					}
				}
		}
		return null;
	}

}
