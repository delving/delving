package org.oclc.oai.harvester.app;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Represent one rown in the table
 * 
 * @author Gerald de Jong <geralddejong@gmai.com>
 */

public class HarvestTask {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Parent parent;
    private HarvestConfig.Harvest harvest;
    private Status status = Status.IDLE;

    public HarvestTask(Parent parent, HarvestConfig.Harvest harvest) {
        this.parent = parent;
        this.harvest = harvest;
        if (harvest.successful == null) {
            harvest.successful = true;
        }
        this.status = harvest.successful ? Status.IDLE : Status.ERROR;
    }

    public Object getColumn(int index) {
        switch (index) {
            case 0: return harvest.id;
            case 1: return FORMAT.format(harvest.lastHarvest);
            case 2: return status;
            case 3: return harvest.output;
            case 4: return harvest.successful;
        }
        throw new RuntimeException("No column "+index);
    }

    public static int getColumnCount() {
        return 5;
    }

    public static TableColumnModel getTableColumnModel(TableCellRenderer tableCellRenderer) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createIdColumn());
        model.addColumn(createStatusColumn());
        model.addColumn(createLastHarvestColumn());
        model.addColumn(createSuccessfulColumn());
        model.addColumn(createOutputColumn());
        for (int walk=0; walk<model.getColumnCount(); walk++) {
            model.getColumn(walk).setCellRenderer(tableCellRenderer);
        }
        return model;
    }

    private static TableColumn createIdColumn() {
        TableColumn column = new TableColumn(0,120);
        column.setHeaderValue("Identifier");
        return column;
    }

    private static TableColumn createStatusColumn() {
        TableColumn column = new TableColumn(2,100);
        column.setHeaderValue("Status");
        return column;
    }

    private static TableColumn createLastHarvestColumn() {
        TableColumn column = new TableColumn(1,200);
        column.setHeaderValue("Last Harvest");
        return column;
    }

    private static TableColumn createSuccessfulColumn() {
        TableColumn column = new TableColumn(4,100);
        column.setHeaderValue("Successful");
        return column;
    }

    private static TableColumn createOutputColumn() {
        TableColumn column = new TableColumn(3,200);
        column.setHeaderValue("Output File");
        return column;
    }

    public static Comparator<HarvestTask> getColumnComparator(int index) {
        switch (index) {
            case 0: return new Comparator<HarvestTask>() {
                public int compare(HarvestTask before, HarvestTask after) {
                    return before.harvest.id.compareTo(after.harvest.id);
                }
            };
            case 1: return new Comparator<HarvestTask>() {
                public int compare(HarvestTask before, HarvestTask after) {
                    return before.harvest.lastHarvest.compareTo(after.harvest.lastHarvest);
                }
            };
            case 2: return new Comparator<HarvestTask>() {
                public int compare(HarvestTask before, HarvestTask after) {
                    return after.status.ordinal() - before.status.ordinal();
                }
            };
            case 3: return new Comparator<HarvestTask>() {
                public int compare(HarvestTask before, HarvestTask after) {
                    return before.harvest.output.compareTo(after.harvest.output);
                }
            };
            case 4: return new Comparator<HarvestTask>() {
                public int compare(HarvestTask before, HarvestTask after) {
                    if (!before.harvest.successful && after.harvest.successful) {
                        return -1;
                    }
                    else if (before.harvest.successful && !after.harvest.successful) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                }
            };
        }
        throw new RuntimeException("No column "+ index);
    }

    public Status getStatus() {
        return status;
    }

    public String toString() {
        return harvest.id;
    }

    public String getOutput() {
        return harvest.output;
    }

    public String getBaseUrl() {
        return harvest.baseUrl;
    }

    public String getLastToken() {
        return harvest.lastToken;
    }

    public boolean hasLastToken() {
        return harvest.lastToken != null && harvest.lastToken.length() > 0;
    }

    public void saveToken(String newToken){
        harvest.lastToken = newToken;
        notifyParent();
    }

    public String getLastHarvestString() {
        return harvest.getLastHarvestString();
    }

    public boolean isSuccessful() {
        return harvest.successful;
    }

    public String getSpec() {
        return harvest.spec;
    }

    public String getPrefix() {
        return harvest.prefix;
    }

    private void notifyParent() {
        if (parent != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    parent.changed(HarvestTask.this);
                }
            });
        }
    }

    public void pending() {
        status = Status.PENDING;
        notifyParent();
    }

    public void finishedSuccessfully() {
        harvest.lastHarvest = new Date();
        harvest.successful = true;
        status = Status.IDLE;
        notifyParent();
    }

    public void finishedUnsuccessfully() {
        harvest.successful = false;
        status = Status.ERROR;
        notifyParent();
    }

    public void abort() {
        status = Status.ABORTED;
        notifyParent();
    }

    public void processing() {
        status = Status.PROCESSING;
        notifyParent();
    }

    public interface Parent {
        void changed(HarvestTask me);
    }

    public static boolean isStartable(Status status) {
        return status == Status.IDLE || status == Status.ABORTED || status == Status.ERROR;
    }

    public enum Status {
        IDLE,
        PENDING,
        ABORTED,
        ERROR,
        PROCESSING,
    }
}
