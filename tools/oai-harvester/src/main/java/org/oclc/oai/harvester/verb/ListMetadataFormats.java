
/**
 Copyright 2006 OCLC, Online Computer Library Center
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.oclc.oai.harvester.verb;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * This class represents an ListMetadataFormats response on either the server or
 * on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListMetadataFormats extends HarvesterVerb {

    public ListMetadataFormats(String baseURL, String identifier) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        super(getRequestURL(baseURL, identifier));
    }
    
    private static String getRequestURL(String baseURL, String identifier) {
        StringBuffer requestURL =  new StringBuffer(baseURL);
        requestURL.append("?verb=ListMetadataFormats");
        if (identifier != null)
            requestURL.append("&identifier=").append(identifier);
        return requestURL.toString();
    }
}
