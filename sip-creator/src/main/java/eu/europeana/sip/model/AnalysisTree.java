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
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A tree representing the statistics gathered
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class AnalysisTree implements Serializable {
    private static final long serialVersionUID = -15171971879119571L;
    private QNameNode root;

    public interface Node extends TreeNode {
        Statistics getStatistics();

        TreePath getTreePath();

        QName getQName();

        void setRecordRoot(QName recordRoot);

        boolean isRecordRoot();

        Iterable<? extends Node> getChildNodes();
    }

    public static int setRecordRoot(DefaultTreeModel model, QName recordRoot) {
        AnalysisTree.Node node = (AnalysisTree.Node) model.getRoot();
        return setRecordRoot(node, recordRoot);
    }

    public static AnalysisTree create(String rootTag) {
        return new AnalysisTree(new QNameNode(rootTag));
    }

    public static AnalysisTree create(List<Statistics> statisticsList, String rootTag, QName recordRoot) {
        QNameNode root = createSubtree(statisticsList, new QNamePath(), recordRoot, null);
        root.setTag(rootTag);
        return new AnalysisTree(root);
    }

    public TreeModel createTreeModel() {
        return new DefaultTreeModel(root, true);
    }

    public void getVariables(List<String> variables) {
        Stack<String> stack = new Stack<String>();
        getVariables(root, false, stack, variables);
    }

    // ==== privates

    private AnalysisTree(QNameNode root) {
        this.root = root;
    }

    private static int setRecordRoot(AnalysisTree.Node node, QName recordRoot) {
        node.setRecordRoot(recordRoot);
        int sum = 0;
        if (node.isRecordRoot()) {
            sum++;
        }
        for (AnalysisTree.Node child : node.getChildNodes()) {
            sum += setRecordRoot(child, recordRoot);
        }
        return sum;
    }

    private static void getVariables(QNameNode node, boolean withinRecord, Stack<String> stack, List<String> variables) {
        if (withinRecord) {
            stack.push(node.toString());
        }
        else if (node.isRecordRoot()) {
            stack.push("input");
            withinRecord = true;
        }
        if (node.getStatistics() != null) {
            if (withinRecord) {
                StringBuilder out = new StringBuilder();
                Iterator<String> tagWalk = stack.iterator();
                while (tagWalk.hasNext()) {
                    String tag = tagWalk.next();
                    out.append(tag);
                    if (tagWalk.hasNext()) {
                        out.append('.');
                    }
                }
                variables.add(out.toString());
            }
        }
        else {
            for (QNameNode child : node.getChildren()) {
                getVariables(child, withinRecord, stack, variables);
            }
        }
        if (withinRecord) {
            stack.pop();
        }
    }

    private static QNameNode createSubtree(List<Statistics> statisticsList, QNamePath path, QName recordRoot, QNameNode parent) {
        Map<QName, List<Statistics>> statisticsMap = new HashMap<QName, List<Statistics>>();
        for (Statistics statistics : statisticsList) {
            QNamePath pp = new QNamePath(statistics.getPath(), path.size());
            if (pp.equals(path)) {
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
        QNameNode node = new QNameNode(parent, name, name != null && name.equals(recordRoot));
        for (Map.Entry<QName, List<Statistics>> entry : statisticsMap.entrySet()) {
            QNamePath childPath = new QNamePath(path);
            childPath.push(entry.getKey());
            Statistics statisticsForChild = null;
            for (Statistics statistics : entry.getValue()) {
                if (statistics.getPath().equals(childPath)) {
                    statisticsForChild = statistics;
                }
            }
            QNameNode child = createSubtree(statisticsList, childPath, recordRoot, node);
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
