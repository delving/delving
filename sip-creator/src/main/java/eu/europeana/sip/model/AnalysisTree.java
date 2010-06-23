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

package eu.europeana.sip.model;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tree representing the statistics gathered
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class AnalysisTree implements Serializable {
    private static final long serialVersionUID = -15171971879119571L;
    private QNameNode root;

    public interface Node extends TreeNode, Comparable<Node> {
        Statistics getStatistics();

        TreePath getTreePath();

        QName getQName();

        boolean setRecordRoot(QName recordRoot);

        boolean setUniqueElement(QName uniqueElemen);

        boolean isRecordRoot();

        boolean isUniqueElement();

        Iterable<? extends Node> getChildNodes();

        boolean couldBeRecordRoot();

        String getVariableName();
    }

    public static void setRecordRoot(DefaultTreeModel model, QName recordRoot) {
        AnalysisTree.Node node = (AnalysisTree.Node) model.getRoot();
        List<AnalysisTree.Node> changedNodes = new ArrayList<AnalysisTree.Node>();
        setRecordRoot(node, recordRoot, changedNodes);
        for (AnalysisTree.Node changedNode : changedNodes) {
            model.nodeChanged(changedNode);
        }
    }

    public static void setUniqueElement(DefaultTreeModel model, QName uniqueElement) {
        AnalysisTree.Node node = (AnalysisTree.Node) model.getRoot();
        List<AnalysisTree.Node> changedNodes = new ArrayList<AnalysisTree.Node>();
        setUniqueElement(node, uniqueElement, changedNodes);
        for (AnalysisTree.Node changedNode : changedNodes) {
            model.nodeChanged(changedNode);
        }
    }

    public static AnalysisTree create(String rootTag) {
        return new AnalysisTree(new QNameNode(rootTag));
    }

    public static AnalysisTree create(List<Statistics> statisticsList, String rootTag) {
        QNameNode root = createSubtree(statisticsList, new QNamePath(), null);
        if (root != null) {
            root.setTag(rootTag);
        }
        else {
            root = new QNameNode("No statistics");
        }
        return new AnalysisTree(root);
    }

    public Node getRoot() {
        return root;
    }

    public void getVariables(List<Node> variables) {
        getVariables(root, false, variables);
    }

    // ==== privates

    private AnalysisTree(QNameNode root) {
        this.root = root;
    }

    private static void setRecordRoot(AnalysisTree.Node node, QName recordRoot, List<Node> changedNodes) {
        if (node.setRecordRoot(recordRoot)) {
            changedNodes.add(node);
        }
        if (recordRoot == null || !node.isRecordRoot()) {
            for (AnalysisTree.Node child : node.getChildNodes()) {
                setRecordRoot(child, recordRoot, changedNodes);
            }
        }
    }       

    private static void setUniqueElement(AnalysisTree.Node node, QName uniqueElement, List<Node> changedNodes) {
        if (node.setUniqueElement(uniqueElement)) {
            changedNodes.add(node);
        }
        if (uniqueElement == null || !node.isUniqueElement()) {
            for (AnalysisTree.Node child : node.getChildNodes()) {
                setUniqueElement(child, uniqueElement, changedNodes);
            }
        }
    }

    private static void getVariables(QNameNode node, boolean withinRecord, List<Node> variables) {
        if (node.isLeaf()) {
            if (withinRecord) {
                variables.add(node);
            }
        }
        else {
            for (QNameNode child : node.getChildren()) {
                getVariables(child, withinRecord || node.isRecordRoot(), variables);
            }
        }
    }

    private static QNameNode createSubtree(List<Statistics> statisticsList, QNamePath path, QNameNode parent) {
        Map<QName, List<Statistics>> statisticsMap = new HashMap<QName, List<Statistics>>();
        for (Statistics statistics : statisticsList) {
            QNamePath subPath = new QNamePath(statistics.getPath(), path.size());
            if (subPath.equals(path) && statistics.getPath().size() == path.size() + 1) {
                QName name = statistics.getPath().getQName(path.size());
                if (name != null) {
                    List<Statistics> list = statisticsMap.get(name);
                    if (list == null) {
                        statisticsMap.put(name, list = new ArrayList<Statistics>());
                    }
                    list.add(statistics);
                }
            }
        }
        if (statisticsMap.isEmpty()) {
            return null;
        }
        QName name = path.peek();
        QNameNode node = new QNameNode(parent, name);
        for (Map.Entry<QName, List<Statistics>> entry : statisticsMap.entrySet()) {
            QNamePath childPath = new QNamePath(path);
            childPath.push(entry.getKey());
            Statistics statisticsForChild = null;
            for (Statistics statistics : entry.getValue()) {
                if (statistics.getPath().equals(childPath)) {
                    statisticsForChild = statistics;
                }
            }
            QNameNode child = createSubtree(statisticsList, childPath, node);
            if (child != null) {
                node.add(child);
                child.setStatistics(statisticsForChild);
            }
            else if (statisticsForChild != null) {
                node.add(new QNameNode(node, statisticsForChild));
            }
        }
        return node;
    }

}
