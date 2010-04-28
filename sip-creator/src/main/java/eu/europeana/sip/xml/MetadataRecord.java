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

import eu.europeana.sip.groovy.GroovyNode;
import eu.europeana.sip.groovy.GroovyNodeList;

/**
 * Something to hold the groovy node and turn it into a string
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetadataRecord {
    private GroovyNode rootNode;

    public MetadataRecord(GroovyNode rootNode) {
        this.rootNode = rootNode;
    }

    public GroovyNode getRootNode() {
        return rootNode;
    }

    public String toString() {
        StringBuilder recordPrinted = new StringBuilder();
        printRecord(rootNode, recordPrinted, 0);
        return recordPrinted.toString();
    }

    private void printRecord(GroovyNode node, StringBuilder out, int depth) {
        if (node.value() instanceof GroovyNodeList) {
            for (int walk = 0; walk < depth; walk++) {
                out.append(' ');
            }
            GroovyNodeList list = (GroovyNodeList) node.value();
            out.append(node.name()).append("\n");
            for (Object member : list) {
                GroovyNode childNode = (GroovyNode) member;
                printRecord(childNode, out, depth + 1);
            }
        }
        else {
            for (int walk = 0; walk < depth; walk++) {
                out.append(' ');
            }
            out.append(node.name()).append(" := ").append(node.value().toString()).append("\n");
        }
    }

    public static String sanitize(String name) {
        return name.replace("-", "_");
    }

}
