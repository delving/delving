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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Something to hold the groovy node and turn it into a string
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetadataRecord {
    private GroovyNode rootNode;
    private int recordNumber;

    public MetadataRecord(GroovyNode rootNode, int recordNumber) {
        this.rootNode = rootNode;
        this.recordNumber = recordNumber;
    }

    public GroovyNode getRootNode() {
        return rootNode;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public List<MetadataVariable> getVariables() {
        List<MetadataVariable> variables = new ArrayList<MetadataVariable>();
        getVariables(rootNode, variables);
        return variables;
    }

    private void getVariables(GroovyNode groovyNode, List<MetadataVariable> variables) {
        if (groovyNode.value() instanceof GroovyNodeList) {
            GroovyNodeList list = (GroovyNodeList) groovyNode.value();
            for (Object member : list) {
                GroovyNode childNode = (GroovyNode) member;
                getVariables(childNode, variables);
            }
        }
        else {
            List<GroovyNode> path = new ArrayList<GroovyNode>();
            GroovyNode walk = groovyNode;
            while (walk != null) {
                path.add(walk);
                walk = walk.parent();
            }
            Collections.reverse(path);
            StringBuilder out = new StringBuilder();
            Iterator<GroovyNode> nodeWalk = path.iterator();
            while (nodeWalk.hasNext()) {
                String nodeName = (String)nodeWalk.next().name();
                out.append(nodeName);
                if (nodeWalk.hasNext()) {
                    out.append('.');
                }
            }
            String variableName = out.toString();
            variables.add(new MetadataVariable(variableName, (String)groovyNode.value()));
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Record #").append(recordNumber).append('\n');
        for (MetadataVariable variable : getVariables()) {
            out.append(variable.toString()).append('\n');
        }
        return out.toString();
    }

//    public String toString() {
//        StringBuilder recordPrinted = new StringBuilder();
//        printRecord(rootNode, recordPrinted, 0);
//        return recordPrinted.toString();
//    }
//
//    private void printRecord(GroovyNode node, StringBuilder out, int depth) {
//        if (node.value() instanceof GroovyNodeList) {
//            for (int walk = 0; walk < depth; walk++) {
//                out.append(' ');
//            }
//            GroovyNodeList list = (GroovyNodeList) node.value();
//            out.append(node.name()).append("\n");
//            for (Object member : list) {
//                GroovyNode childNode = (GroovyNode) member;
//                printRecord(childNode, out, depth + 1);
//            }
//        }
//        else {
//            for (int walk = 0; walk < depth; walk++) {
//                out.append(' ');
//            }
//            out.append(node.name()).append(" := ").append(node.value().toString()).append("\n");
//        }
//    }
}
