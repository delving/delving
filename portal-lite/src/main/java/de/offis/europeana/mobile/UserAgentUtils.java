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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides some helper methods to support desktop browser detection.
 * This is a modified java version of the "SimpleDesktop Matching Engine" of http://www.tera-wurfl.com
 *  
 * 
 * @author Dennis Heinen <Dennis.Heinen@offis.de>
 *
 */
public class UserAgentUtils {

	private static String[] MOBILE_BROWSERS = new String[] { "cldc",
			"symbian", "midp", "j2me", "mobile", "wireless", "palm", "phone",
			"pocket pc", "pocketpc", "netfront", "bolt", "iris", "brew",
			"openwave", "windows ce", "wap2.", "android", "opera mini",
			"opera mobi", "maemo", "fennec", "blazer" };
	private static String[] DESKTOP_BROWSERS = new String[] { "slcc1",
			".net clr", "trident/4", "media center pc", "funwebproducts",
			"macintosh", "wow64", "aol 9.", "america online browser",
			"googletoolbar" };

	/**
	 * Determines whether a specified UserAgent contains a word that is common for mobile browsers (or not)
	 * 
	 * @param userAgent The UserAgent
	 * @return a flag that indicates whether the specified UserAgent contains a word that is common for mobile browsers (or not)
	 */
	public static boolean isMobileBrowser(String userAgent) {
		if (userAgent != null) {
			String lowerUA = userAgent.toLowerCase();
			for (String browser_signature: MOBILE_BROWSERS){
				if (lowerUA.contains(browser_signature)){
					return true;
				}
			}
			// ARM Processor
			if (regexContains(userAgent, "/armv[5-9][l0-9]/")) {
				return true;
			}
			// Screen resolution in UA
			if (regexContains(userAgent, "/[^\\d]\\d{3}x\\d{3}/")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether a specified UserAgent contains a word that is common for desktop browsers (or not)
	 * 
	 * @param userAgent The UserAgent
	 * @return a flag that indicates whether the specified UserAgent contains a word that is common for desktop browsers (or not)
	 */
	public static boolean isDesktopBrowser(String userAgent) {
		if (userAgent != null) {
			String lowerUA = userAgent.toLowerCase();
			for (String browser_signature: DESKTOP_BROWSERS){
				if (lowerUA.contains(browser_signature)){
					return true;
				}
			}
		}
		return false;
	}	

	/**
	 * regex helper method
	 * 
	 * @param userAgent
	 * @param regex
	 * @return The specified userAgent matches the regex
	 */
	public static boolean regexContains(String userAgent, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(userAgent);
		// try to find a match
		return m.find();
	}
}
