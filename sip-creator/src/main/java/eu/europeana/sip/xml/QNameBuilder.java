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

package eu.europeana.sip.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * build qnames
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QNameBuilder {
    private static final String[][] PREFIX = {
            {"dc", "http://purl.org/dc/elements/1.1/"},
            {"europeana", "http://www.europeana.eu"},
            {"dcterms", "http://purl.org/dc/terms/"}
    };

    public static void writeNamespaces(XMLStreamWriter writer) throws XMLStreamException {
        for (String[] pair : QNameBuilder.PREFIX) {
            writer.writeNamespace(pair[0], pair[1]);
        }
    }

    public static QName createQName(String key) {
        int colon = key.indexOf(':');
        if (colon > 0) {
            String prefix = key.substring(0, colon);
            String localPart = key.substring(colon + 1);
            String namespace = null;
            for (String[] pair : PREFIX) {
                if (prefix.equals(pair[0])) {
                    namespace = pair[1];
                }
            }
            if (namespace == null) {
                throw new IllegalStateException("Namespace not found for: " + prefix);
            }
            return new QName(namespace, localPart, prefix);
        }
        else {
            return new QName(key);
        }
    }
}