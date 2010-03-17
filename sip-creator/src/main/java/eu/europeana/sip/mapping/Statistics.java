package eu.europeana.sip.mapping;

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
    private static final DecimalFormat PERCENT = new DecimalFormat("#0.00%");
    private static final long serialVersionUID = 2187793718415968490L;
    private static final int MAXIMUM_LENGTH = 100;
    private QNamePath path;
    private int total;
    private boolean unique = true;
    private Map<String, CounterImpl> counterMap = new TreeMap<String, CounterImpl>();

    public Statistics(QNamePath path) {
        this.path = path;
    }

    public void recordValue(String value) {
        if (value.length() > MAXIMUM_LENGTH) {
            value = "** LONGER THAN 100 **";
        }
        CounterImpl counter = counterMap.get(value);
        if (counter == null) {
            counterMap.put(value, counter = new CounterImpl(value));
        }
        else {
            unique = false;
        }
        counter.increment();
        total++;
    }

    public QNamePath getPath() {
        return path;
    }

    public boolean isUnique() {
        return unique;
    }

    public int getTotal() {
        return total;
    }

    public void trimTo(int count) {
        List<CounterImpl> counterList = new ArrayList<CounterImpl>(counterMap.values());
        if (!unique) {
            Collections.sort(counterList);
        }
        counterMap.clear();
        for (CounterImpl counter : counterList) {
            if (count-- == 0) {
                break;
            }
            counterMap.put(counter.getValue(), counter);
        }
    }

    public List<? extends Counter> getCounters() {
        List<CounterImpl> counterList = new ArrayList<CounterImpl>(counterMap.values());
        if (!unique) {
            Collections.sort(counterList);
        }
        return counterList;
    }

    public String toString() {
        return path + " (" + total + ") "+ (unique ? "unique" : "non-unique");
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
            double percent = (double)count / total;
            return PERCENT.format(percent);
        }

        @Override
        public int compareTo(CounterImpl counter) {
            return counter.count - count;
        }

        public String toString() {
            return count + " [" + value + "] " + getPercentage();
        }
    }
}
