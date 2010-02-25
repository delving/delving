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

/**
 * This class tries to identify desktop browsers with the help of the UserAgent and some helper methods
 * This is a modified java version of the "SimpleDesktop Matching Engine" of http://www.tera-wurfl.com
 * 
 * 
 * @author Dennis Heinen <Dennis.Heinen@offis.de>
 * 
 */
public class SimpleDesktopUserAgentMatcher {

	private static String[] _userAgentParts = new String[] { "Chrome",
			"yahoo.com", "google.com", "Comcast" };

	/**
	 * Determines whether a specified UserAgent represents a desktop browser (or not)
	 * 
	 * @param userAgent The UserAgent
	 * @return a flag that indicates whether the specified UserAgent represents a desktop browser (or not)
	 */
	public static boolean isDesktopBrowser(String userAgent) {
		if (userAgent != null) {
			if (UserAgentUtils.isMobileBrowser(userAgent))
				return false;
			if (userAgent.contains("Firefox") && 
					!userAgent.contains("Tablet"))
				return true;
			if (UserAgentUtils.isDesktopBrowser(userAgent))
				return true;
			if (UserAgentUtils.regexContains(userAgent, "/^Mozilla\\/4\\.0 \\(compatible; MSIE \\d.\\d; Windows NT \\d.\\d/"))
				return true;
			for (String part : _userAgentParts) {
				if (userAgent.contains(part)) {
					return true;
				}
			}
		}
		return false;
	}
	
}