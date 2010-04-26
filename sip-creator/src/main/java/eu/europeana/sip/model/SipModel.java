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

import eu.europeana.definitions.annotations.AnnotationProcessor;

import javax.swing.ListModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This model is behind the whole sip creator, as a facade for all the models related to a FileSet
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SipModel {
    private FieldListModel fieldListModel;
    private FileSet fileSet;
    private List<Statistics> statistics;
    private AnalysisTree analysisTree;
    private QName recordRoot;
    private DefaultTreeModel analysisTreeModel;
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();

    public SipModel() {
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
    }

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.fieldListModel = new FieldListModel(annotationProcessor);
    }

    public void setFileSet(FileSet fileSet) throws IOException {
        this.fileSet = fileSet;
        this.statistics = fileSet.getStatistics();
        this.recordRoot = fileSet.getRecordRoot();
        if (recordRoot != null) {
            if (statistics != null) {
                analysisTree = AnalysisTree.create(statistics, fileSet.getName(), recordRoot);
            }
            else {
                analysisTree = AnalysisTree.create("Analysis not yet performed"); 
            }
            analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
            List<String> variables = new ArrayList<String>();
            analysisTree.getVariables(variables);
            variableListModel.setList(variables);
        }
        // todo: remove
        try {
            codeDocument.insertString(0, "Code", null);
            outputDocument.insertString(0, "Output", null);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public TreeModel getAnalysisTreeModel() {
        return analysisTreeModel;
    }

    public void selectNode(AnalysisTree.Node node) {
        statisticsTableModel.setCounterList(node.getStatistics().getCounters());
    }

    public void setRecordRoot(QName recordRoot) {
        this.recordRoot = recordRoot;
        // todo: invalidate the variable list model!
    }

    public TableModel getStatisticsTableModel() {
        return statisticsTableModel;
    }

    public ListModel getFieldListModel() {
        return fieldListModel;
    }

    public ListModel getVariablesListModel() {
        return variableListModel;
    }

    public Document getCodeDocument() {
        return codeDocument;
    }

    public Document getOutputDocument() {
        return outputDocument;
    }
}
