/*
 * Copyright 2010 DELVING BV
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

package eu.delving.core.metadata;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tree representing the statistics gathered
 *
 * @author Gerald de Jong, Delving BV, <geralddejong@gmail.com>
 */

public class AnalysisTree implements Serializable {
    private static final long serialVersionUID = -15171971879119571L;
    private AnalysisTreeNode root;

    public interface Node extends TreeNode, Comparable<Node> {
        Statistics getStatistics();

        TreePath getTreePath();

        Tag getTag();

        Path getPath();

        boolean setRecordRoot(Path recordRoot);

        boolean setUniqueElement(Path uniqueElement);

        boolean isRecordRoot();

        boolean isUniqueElement();

        Iterable<? extends Node> getChildNodes();

        boolean couldBeRecordRoot();

        String getVariableName();
    }

    public static void setRecordRoot(DefaultTreeModel model, Path recordRoot) {
        AnalysisTree.Node node = (AnalysisTree.Node) model.getRoot();
        List<AnalysisTree.Node> changedNodes = new ArrayList<AnalysisTree.Node>();
        setRecordRoot(node, recordRoot, changedNodes);
        for (AnalysisTree.Node changedNode : changedNodes) {
            model.nodeChanged(changedNode);
        }
    }

    public static void setUniqueElement(DefaultTreeModel model, Path uniqueElement) {
        AnalysisTree.Node node = (AnalysisTree.Node) model.getRoot();
        List<AnalysisTree.Node> changedNodes = new ArrayList<AnalysisTree.Node>();
        setUniqueElement(node, uniqueElement, changedNodes);
        for (AnalysisTree.Node changedNode : changedNodes) {
            model.nodeChanged(changedNode);
        }
    }

    public static AnalysisTree create(String rootTag) {
        return new AnalysisTree(new AnalysisTreeNode(Tag.create(rootTag)));
    }

    public static AnalysisTree create(List<Statistics> statisticsList) {
        AnalysisTreeNode root = createSubtree(statisticsList, new Path(), null);
        if (root == null) {
            root = new AnalysisTreeNode(Tag.create("No statistics"));
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

    private AnalysisTree(AnalysisTreeNode root) {
        this.root = root;
    }

    private static void setRecordRoot(AnalysisTree.Node node, Path recordRoot, List<Node> changedNodes) {
        if (node.setRecordRoot(recordRoot)) {
            changedNodes.add(node);
        }
        if (recordRoot == null || !node.isRecordRoot()) {
            for (AnalysisTree.Node child : node.getChildNodes()) {
                setRecordRoot(child, recordRoot, changedNodes);
            }
        }
    }

    private static void setUniqueElement(AnalysisTree.Node node, Path uniqueElement, List<Node> changedNodes) {
        if (node.setUniqueElement(uniqueElement)) {
            changedNodes.add(node);
        }
        if (uniqueElement == null || !node.isUniqueElement()) {
            for (AnalysisTree.Node child : node.getChildNodes()) {
                setUniqueElement(child, uniqueElement, changedNodes);
            }
        }
    }

    private static void getVariables(AnalysisTreeNode node, boolean withinRecord, List<Node> variables) {
        if (node.isLeaf()) {
            if (withinRecord) {
                variables.add(node);
            }
        }
        else {
            for (AnalysisTreeNode child : node.getChildren()) {
                getVariables(child, withinRecord || node.isRecordRoot(), variables);
            }
        }
    }

    private static AnalysisTreeNode createSubtree(List<Statistics> statisticsList, Path path, AnalysisTreeNode parent) {
        Map<Tag, List<Statistics>> statisticsMap = new HashMap<Tag, List<Statistics>>();
        for (Statistics statistics : statisticsList) {
            Path subPath = new Path(statistics.getPath(), path.size());
            if (subPath.equals(path) && statistics.getPath().size() == path.size() + 1) {
                Tag tag = statistics.getPath().getTag(path.size());
                if (tag != null) {
                    List<Statistics> list = statisticsMap.get(tag);
                    if (list == null) {
                        statisticsMap.put(tag, list = new ArrayList<Statistics>());
                    }
                    list.add(statistics);
                }
            }
        }
        if (statisticsMap.isEmpty()) {
            return null;
        }
        Tag tag = path.peek();
        AnalysisTreeNode node = tag == null ? null : new AnalysisTreeNode(parent, tag);
        for (Map.Entry<Tag, List<Statistics>> entry : statisticsMap.entrySet()) {
            Path childPath = new Path(path);
            childPath.push(entry.getKey());
            Statistics statisticsForChild = null;
            for (Statistics statistics : entry.getValue()) {
                if (statistics.getPath().equals(childPath)) {
                    statisticsForChild = statistics;
                }
            }
            AnalysisTreeNode child = createSubtree(statisticsList, childPath, node);
            if (child != null) {
                if (node == null) {
                    node = child;
                }
                else {
                    node.add(child);
                }
                child.setStatistics(statisticsForChild);
            }
            else if (statisticsForChild != null) {
                if (node == null) {
                    node = new AnalysisTreeNode(node, statisticsForChild);
                }
                else {
                    node.add(new AnalysisTreeNode(node, statisticsForChild));
                }
            }
        }
        return node;
    }

}
