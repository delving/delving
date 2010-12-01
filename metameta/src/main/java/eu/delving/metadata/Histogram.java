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

package eu.delving.metadata;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Use some adjacent primes to check for uniqueness of strings based on hashCode
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Histogram implements Serializable {
    private static final DecimalFormat PERCENT = new DecimalFormat("#0.00%");
    private int maxStorageSize;
    private int total;
    private int storageSize;
    private Map<String, Counter> counterMap = new TreeMap<String, Counter>();

    public Histogram(int maxStorageSize) {
        this.maxStorageSize = maxStorageSize;
    }

    public void recordValue(String value) {
        Counter counter = counterMap.get(value);
        if (counter == null) {
            counterMap.put(value, counter = new Counter(value));
            storageSize += value.length();
        }
        counter.count++;
        total++;
    }

    public int getTotal() {
        return total;
    }

    public int getSize() {
        return counterMap.size();
    }

    public Set<String> getValues() {
        return counterMap.keySet();
    }

    public Collection<Counter> getCounters() {
        return counterMap.values();
    }

    public boolean isStorageOverflow() {
        return storageSize > maxStorageSize;
    }

    public class Counter implements Comparable<Counter>, Serializable {
        private String value;
        private int count;

        public Counter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCount() {
            return count;
        }

        public String getPercentage() {
            double percent = (double) count / total;
            return PERCENT.format(percent);
        }

        @Override
        public int compareTo(Counter counter) {
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