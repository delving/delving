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

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class represents an ListRecords response on either the server or
 * on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */

public class ListRecords extends HarvesterVerb {
    public ListRecords(String baseURL, String from, String until, String set, String metadataPrefix)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        super(getRequestURL(baseURL, from, until, set, metadataPrefix));
    }

    public ListRecords(String baseURL, String resumptionToken, String set, String metadataPrefix)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        super(getRequestURL(baseURL, resumptionToken, set, metadataPrefix));
    }

    private static String getRequestURL(String baseURL, String resumptionToken, String set, String metadataPrefix) throws UnsupportedEncodingException {
        StringBuffer requestURL = new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        if (resumptionToken == null || resumptionToken.isEmpty()) {
            if (metadataPrefix != null) requestURL.append("&metadataPrefix=").append(metadataPrefix);
            if (set != null) requestURL.append("&set=").append(set);
        }
        else {
            requestURL.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, "UTF-8"));
        }
        return requestURL.toString();
    }

    private static String getRequestURL(String baseURL, String from, String until, String set, String metadataPrefix) {
        StringBuffer requestURL = new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        if (from != null && !from.equals("1970-01-01")) requestURL.append("&from=").append(from);
        if (until != null) requestURL.append("&until=").append(until);
        if (set != null) requestURL.append("&set=").append(set);
        requestURL.append("&metadataPrefix=").append(metadataPrefix);
        return requestURL.toString();
    }

    public String getResumptionToken() throws TransformerException, NoSuchFieldException {
        String schemaLocation = getSchemaLocation();
        try {
            if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
                return getSingleString("/oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken");
            }
            else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
                return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:resumptionToken");
            }
            else {
                throw new NoSuchFieldException(schemaLocation);
            }
        }
        catch (Exception e) {
            return null;
        }
    }

}
