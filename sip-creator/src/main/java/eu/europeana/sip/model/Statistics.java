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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Maintain a map of strings and counters
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Statistics implements Comparable<Statistics>, Serializable {
    private static final long serialVersionUID = 21772426262368490L;
    private static final int MAX_STATISTICS_LIST_SIZE = 1000;
    private static final DecimalFormat PERCENT = new DecimalFormat("#0.00%");
    private static final int MAXIMUM_LENGTH = 1000;
    private QNamePath path;
    private int total;
    private Map<String, CounterImpl> counterMap = new TreeMap<String, CounterImpl>();

    public Statistics(QNamePath path) {
        this.path = path;
    }

    public void recordValue(String value) {
        if (value.length() > MAXIMUM_LENGTH) {
            value = value.substring(0, MAXIMUM_LENGTH) + " [...longer...]";
        }
        CounterImpl counter = counterMap.get(value);
        if (counter == null) {
            counterMap.put(value, counter = new CounterImpl(value));
        }
        counter.increment();
    }

    public void recordOccurrence() {
        total++;
    }

    public QNamePath getPath() {
        return path;
    }

    public int getTotal() {
        return total;
    }

    public void trim(boolean complete) {
        if (complete || counterMap.size() > MAX_STATISTICS_LIST_SIZE * 3) {
            List<CounterImpl> counterList = new ArrayList<CounterImpl>(counterMap.values());
            counterMap.clear();
            int count = MAX_STATISTICS_LIST_SIZE;
            for (CounterImpl counter : counterList) {
                if (count-- == 0) {
                    break;
                }
                counterMap.put(counter.getValue(), counter);
            }
        }
    }

    public List<? extends Counter> getCounters() {
        List<CounterImpl> counterList = new ArrayList<CounterImpl>(counterMap.values());
//        counterList.add(new CounterImpl(" Total Occurrences", total));
        Collections.sort(counterList);
        return counterList;
    }

    public boolean isEmpty() {
        return counterMap.isEmpty();
    }

    public String toString() {
        return path + " (" + total + ")";
    }

    @Override
    public int compareTo(Statistics statistics) {
        return path.compareTo(statistics.path);
    }

    public interface Counter {
        String getValue();

        int getCount();

        String getPercentage();
    }

    private class CounterImpl implements Comparable<CounterImpl>, Counter, Serializable {
        private static final long serialVersionUID = 8723534933008189272L;
        private String value;
        private int count;

        public CounterImpl(String value, int count) {
            this.value = value;
            this.count = count;
        }

        public CounterImpl(String value) {
            this.value = value;
        }

        public void increment() {
            count++;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public String getPercentage() {
            double percent = (double) count / total;
            return PERCENT.format(percent);
        }

        @Override
        public int compareTo(CounterImpl counter) {
            int diff = counter.count - count;
            if (diff == 0) {
                return value.compareTo(counter.value);
            }
            return diff;
        }

        public String toString() {
            return count + " [" + value + "] " + getPercentage();
        }
    }
}
