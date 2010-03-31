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

package eu.europeana.sip.gui;

import eu.europeana.sip.mapping.Statistics;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Translate a list of counters to a table model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StatisticsCounterTableModel extends AbstractTableModel {
    private List<? extends Statistics.Counter> counterList;

    public StatisticsCounterTableModel(List<? extends Statistics.Counter> counterList) {
        this.counterList = counterList;
    }

    @Override
    public int getRowCount() {
        if (counterList == null) {
            return 0;
        }
        return counterList.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Statistics.Counter counter = counterList.get(row);
        switch (col) {
            case 0:
                return counter.getPercentage();
            case 1:
                return counter.getCount();
            case 2:
                return counter.getValue();
            default:
                throw new IllegalArgumentException();
        }
    }
}

