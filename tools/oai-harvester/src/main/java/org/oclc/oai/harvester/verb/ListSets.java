
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * This class represents an ListSets response on either the server or
 * on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListSets extends HarvesterVerb {
    /**
     * Mock object constructor (for unit testing purposes)
     */
    public ListSets() throws ParserConfigurationException, TransformerConfigurationException {
        super();
    }
    
    /**
     * Client-side ListSets verb constructor
     *
     * @param baseURL the baseURL of the server to be queried
     * @exception IOException an I/O error occurred
     */
    public ListSets(String baseURL)
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException {
        super(getRequestURL(baseURL));
    }
    
    /**
     * Get the oai:resumptionToken from the response
     * 
     * @return the oai:resumptionToken as a String
     * @throws TransformerException
     * @throws NoSuchFieldException
     */
    public String getResumptionToken()
    throws TransformerException, NoSuchFieldException {
        if (SCHEMA_LOCATION_V2_0.equals(getSchemaLocation())) {
            return getSingleString("/oai20:OAI-PMH/oai20:ListSets/oai20:resumptionToken");
        } else if (SCHEMA_LOCATION_V1_1_LIST_SETS.equals(getSchemaLocation())) {
            return getSingleString("/oai11_ListSets:ListSets/oai11_ListSets:resumptionToken");
        } else {
            throw new NoSuchFieldException(getSchemaLocation());
        }
    }
    
    /**
     * Generate a ListSets request for the given baseURL
     * 
     * @param baseURL
     * @return
     */
    private static String getRequestURL(String baseURL) {
        StringBuffer requestURL =  new StringBuffer(baseURL);
        requestURL.append("?verb=ListSets");
        return requestURL.toString();
    }
}
