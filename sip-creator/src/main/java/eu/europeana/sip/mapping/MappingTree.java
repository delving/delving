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

package eu.europeana.sip.mapping;

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

    public interface Node extends TreeNode {
        Statistics getStatistics();
        TreePath getTreePath();
        QName getQName();
        void setRecordRoot(QName recordRoot);
        boolean isRecordRoot();
        Iterable<? extends Node> getChildNodes();
    }

    public static int setRecordRoot(DefaultTreeModel model, QName recordRoot) {
        MappingTree.Node node = (MappingTree.Node)model.getRoot();
        return setRecordRoot(node, recordRoot);
    }

    private static int setRecordRoot(MappingTree.Node node, QName recordRoot) {
        node.setRecordRoot(recordRoot);
        int sum = 0;
        if (node.isRecordRoot()) {
            sum++;
        }
        for (MappingTree.Node child : node.getChildNodes()) {
            sum += setRecordRoot(child, recordRoot);
        }
        return sum;
    }

    public MappingTree(QNameNode root) {
        this.root = root;
    }

    public TreeModel createTreeModel() {
        return new DefaultTreeModel(root, true);
    }

    private static class QNameNode implements Node, Serializable {
        private static final long serialVersionUID = -8362212829296408316L;
        private QNameNode parent;
        private List<QNameNode> children = new ArrayList<QNameNode>();
        private String tag;
        private QName qName;
        private boolean recordRoot;
        private Statistics statistics;

        private QNameNode(String tag) {
            this.tag = tag;
        }

        private QNameNode(QNameNode parent, QName qName, boolean recordRoot) {
            this.parent = parent;
            this.qName = qName;
            this.recordRoot = recordRoot;
        }

        private QNameNode(QNameNode parent, Statistics statistics) {
            this.parent = parent;
            this.statistics = statistics;
            this.qName = statistics.getPath().peek();
        }

        public void setStatistics(Statistics statistics) {
            this.statistics = statistics;
        }

        @Override
        public Statistics getStatistics() {
            return statistics;
        }

        @Override
        public TreePath getTreePath() {
            List<QNameNode> list = new ArrayList<QNameNode>();
            compilePathList(list);
            return new TreePath(list.toArray());
        }

        @Override
        public QName getQName() {
            return qName;
        }

        @Override
        public void setRecordRoot(QName recordRoot) {
            this.recordRoot = qName != null && qName.equals(recordRoot); 
        }

        @Override
        public boolean isRecordRoot() {
            return recordRoot;
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
            if (qName == null) {
                return tag;
            }
            else if (!qName.getPrefix().isEmpty()) {
                return qName.getPrefix() + ':' + qName.getLocalPart();
            }
            else {
                return qName.getLocalPart();
            }
        }
    }

    public static MappingTree create(String rootTag) {
        return new MappingTree(new QNameNode(rootTag));
    }

    public static MappingTree create(List<Statistics> statisticsList, String rootTag, QName recordRoot) {
        QNameNode root = createSubtree(statisticsList, new QNamePath(), recordRoot, null);
        root.tag = rootTag;
        return new MappingTree(root);
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
