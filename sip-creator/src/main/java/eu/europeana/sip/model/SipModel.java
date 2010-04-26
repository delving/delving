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
import eu.europeana.sip.xml.AnalysisParser;

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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This model is behind the whole sip creator, as a facade for all the models related to a FileSet
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SipModel {
    private FieldListModel fieldListModel;
    private FileSet fileSet;
    private List<Statistics> statisticsList;
    private AnalysisTree analysisTree;
    private QName recordRoot;
    private DefaultTreeModel analysisTreeModel;
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();
    private List<FileSetListener> fileSetListeners = new CopyOnWriteArrayList<FileSetListener>();

    public interface FileSetListener {
        void updatedFileSet();
    }

    public interface AnalysisListener {
        void finished(boolean success);
        void analysisProgress(long elementCount);
    }

    public SipModel() {
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
    }

    public void addFileSetListener(FileSetListener fileSetListener) {
        fileSetListeners.add(fileSetListener);
    }

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.fieldListModel = new FieldListModel(annotationProcessor);
    }

    public void setFileSet(FileSet fileSet) throws IOException {
        this.fileSet = fileSet;
        setStatisticsList(fileSet.getStatistics());
        setRecordRoot(fileSet.getRecordRoot());
        // todo: remove
        try {
            codeDocument.insertString(0, "Code", null);
            outputDocument.insertString(0, "Output", null);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        for (FileSetListener fileSetListener : fileSetListeners) {
            fileSetListener.updatedFileSet();
        }
    }

    public void analyze(final AnalysisListener listener) {
        fileSet.analyze(new AnalysisParser.Listener() {

            @Override
            public void success(List<Statistics> list) {
                setStatisticsList(list);
                listener.finished(true);
            }

            @Override
            public void failure(Exception exception) {
                listener.finished(false);
            }

            @Override
            public void progress(long elementCount) {
                listener.analysisProgress(elementCount);
            }
        });
    }

    public TreeModel getAnalysisTreeModel() {
        return analysisTreeModel;
    }

    public void selectNode(AnalysisTree.Node node) {
        if (node.getStatistics() != null) {
            statisticsTableModel.setCounterList(node.getStatistics().getCounters());
        }
    }

    public void setRecordRoot(QName recordRoot) {
        this.recordRoot = recordRoot;
        if (recordRoot != null) {
            List<String> variables = new ArrayList<String>();
            analysisTree.getVariables(variables);
            variableListModel.setList(variables);
        }
        else {
            variableListModel.setList(null);
        }
    }

    public TableModel getStatisticsTableModel() {
        return statisticsTableModel;
    }

    public long getElementCount() {
        if (statisticsList != null) {
            long total = 0;
            for (Statistics stats : statisticsList) {
                total += stats.getTotal();
            }
            return total;
        }
        else {
            return 0L;
        }
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

    // === privates

    private void setStatisticsList(List<Statistics> statisticsList) {
        this.statisticsList = statisticsList;
        if (statisticsList != null) {
            analysisTree = AnalysisTree.create(statisticsList, fileSet.getName(), recordRoot);
        }
        else {
            analysisTree = AnalysisTree.create("Analysis not yet performed");
        }
        analysisTreeModel.setRoot(analysisTree.getRoot());
    }
}
