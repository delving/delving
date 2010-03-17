package eu.europeana.sip.xml;

import javax.xml.namespace.QName;

/**
 * build qnames
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QNameBuilder {
    public static final String[][] PREFIX = {
            {"dc", "http://purl.org/dc/elements/1.1/"},
            {"europeana", "http://www.europeana.eu"},
            {"dcterms", "http://purl.org/dc/terms/"}
    };

    public static QName createQName(String key) {
        int colon = key.indexOf(':');
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
}