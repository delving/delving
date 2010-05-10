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

import eu.europeana.sip.xml.MetadataRecord;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A node of the analysis tree
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QNameNode implements AnalysisTree.Node, Serializable {
    private static final long serialVersionUID = -8362212829296408316L;
    private QNameNode parent;
    private List<QNameNode> children = new ArrayList<QNameNode>();
    private String tag;
    private QName qName;
    private boolean recordRoot;
    private Statistics statistics;

    QNameNode(String tag) {
        this.tag = tag;
    }

    QNameNode(QNameNode parent, QName qName) {
        this.parent = parent;
        this.qName = qName;
    }

    QNameNode(QNameNode parent, Statistics statistics) {
        this.parent = parent;
        this.statistics = statistics;
        this.qName = statistics.getPath().peek();
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public List<QNameNode> getChildren() {
        return children;
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
    public boolean setRecordRoot(QName recordRoot) {
        boolean oldRecordRoot = this.recordRoot;
        this.recordRoot = recordRoot != null && qName != null && qName.equals(recordRoot);
        return this.recordRoot != oldRecordRoot;
    }

    @Override
    public boolean isRecordRoot() {
        return recordRoot;
    }

    @Override
    public Iterable<? extends AnalysisTree.Node> getChildNodes() {
        return children;
    }

    @Override
    public boolean couldBeRecordRoot() {
        return statistics != null && statistics.getCounters().isEmpty();
    }

    @Override
    public String getVariableName() {
        List<QNameNode> path = new ArrayList<QNameNode>();
        QNameNode node = this;
        while (node != null && !node.isRecordRoot()) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        StringBuilder out = new StringBuilder("input.");
        Iterator<QNameNode> tagWalk = path.iterator();
        while (tagWalk.hasNext()) {
            String tag = tagWalk.next().toString();
            out.append(MetadataRecord.sanitize(tag));
            if (tagWalk.hasNext()) {
                out.append('.');
            }
        }
        return out.toString();
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
        QNameNode qNameNode = (QNameNode) treeNode;
        return children.indexOf(qNameNode);
    }

    @Override
    public boolean getAllowsChildren() {
        return statistics != null && statistics.getCounters().isEmpty();
//        return !children.isEmpty();
    }

    @Override
    public boolean isLeaf() {
        return statistics != null && !statistics.getCounters().isEmpty();
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
            return qName.getPrefix() + '_' + qName.getLocalPart();
        }
        else {
            return qName.getLocalPart();
        }
    }
}
