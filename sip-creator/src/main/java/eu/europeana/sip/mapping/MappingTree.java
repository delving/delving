package eu.europeana.sip.mapping;

import eu.europeana.sip.schema.SourceSelection;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A tree representing the statistics gathered
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class MappingTree implements Serializable {
    private static final long serialVersionUID = -15171971879119571L;
    private QNameNode root;

    public interface Node {
        Statistics getStatistics();
        SourceSelection getDomain();
        TreePath getTreePath();
        Iterable<? extends Node> getChildNodes();
    }

    public MappingTree(QNameNode root) {
        this.root = root;
    }

    public TreeModel createTreeModel() {
        return new DefaultTreeModel(root, true);
    }

    private static class QNameNode implements TreeNode, Node, Serializable {
        private static final long serialVersionUID = -8362212829296408316L;
        private QNameNode parent;
        private List<QNameNode> children = new ArrayList<QNameNode>();
        private String tag;
        private QName name;
        private Statistics statistics;
        private SourceSelection sourceSelection;

        private QNameNode(String tag) {
            this.tag = tag;
        }

        private QNameNode(QNameNode parent, QName name) {
            this.parent = parent;
            this.name = name;
        }

        private QNameNode(QNameNode parent, Statistics statistics) {
            this.parent = parent;
            this.statistics = statistics;
            this.name = statistics.getPath().peek();
            this.sourceSelection = new SourceSelection(statistics.getPath().toString());
        }

        public void setStatistics(Statistics statistics) {
            this.statistics = statistics;
        }

        @Override
        public Statistics getStatistics() {
            return statistics;
        }

        @Override
        public SourceSelection getDomain() {
            return sourceSelection;
        }

        @Override
        public TreePath getTreePath() {
            List<QNameNode> list = new ArrayList<QNameNode>();
            compilePathList(list);
            return new TreePath(list.toArray());
        }

        @Override
        public Iterable<? extends Node> getChildNodes() {
            return children;
        }

        private void compilePathList(List<QNameNode> list) {
            if (parent != null) {
                parent.compilePathList(list);
            }
            list.add(this);
        }

        public void add(QNameNode child) {
            children.add(child);
        }

        @Override
        public TreeNode getChildAt(int index) {
            return children.get(index);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode treeNode) {
            QNameNode qNameNode = (QNameNode)treeNode;
            return children.indexOf(qNameNode);
        }

        @Override
        public boolean getAllowsChildren() {
            return !children.isEmpty();
        }

        @Override
        public boolean isLeaf() {
            return statistics != null;
        }

        @Override
        public Enumeration children() {
            return new Vector<QNameNode>(children).elements();
        }

        public String toString() {
            if (name == null) {
                return tag;
            }
            else if (!name.getPrefix().isEmpty()) {
                return name.getPrefix() + ':' + name.getLocalPart();
            }
            else {
                return name.getLocalPart();
            }
        }
    }

    public static MappingTree create(String rootTag) {
        return new MappingTree(new QNameNode(rootTag));
    }

    public static MappingTree create(List<Statistics> statisticsList, String rootTag) {
        QNameNode root = createSubtree(statisticsList, new QNamePath(), null);
        root.tag = rootTag;
        return new MappingTree(root);
    }

    private static QNameNode createSubtree(List<Statistics> statisticsList, QNamePath path, QNameNode parent) {
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
        QNameNode node = new QNameNode(parent, path.peek());
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
