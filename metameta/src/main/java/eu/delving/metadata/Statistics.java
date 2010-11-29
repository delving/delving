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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maintain a map of strings and counters
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Statistics implements Comparable<Statistics>, Serializable {
    private Path path;
    private int total;
    private ValueStats valueStats;

    public Statistics(Path path) {
        this.path = path;
    }

    public void recordValue(String value) {
        if (valueStats == null) {
            valueStats = new ValueStats();
        }
        valueStats.recordValue(value);
    }

    public void recordOccurrence() {
        total++;
    }

    public Path getPath() {
        return path;
    }

    public int getTotal() {
        return total;
    }

    public boolean hasValues() {
        return valueStats != null;
    }

    public RandomSample getRandomSample() {
        if (valueStats == null) return null;
        return valueStats.randomSample;
    }

    public Histogram getHistogram() {
        if (valueStats == null) return null;
        return valueStats.histogram;
    }

    public Uniqueness getUniqueness() {
        if (valueStats == null) return null;
        return valueStats.uniqueness;
    }

    public void finish() {
        if (valueStats != null) {
            valueStats.finish();
        }
    }

    public String toString() {
        return path + " (" + total + ")";
    }

    @Override
    public int compareTo(Statistics statistics) {
        return path.compareTo(statistics.path);
    }

    public String toHtml() {
        if (valueStats == null) {
            return String.format("<html><p>%s appears %d times with no values</p>", path, total);
        }
        else {
            return valueStats.toHtml();
        }
    }

    private class ValueStats implements Serializable {
        RandomSample randomSample = new RandomSample(100);
        Histogram histogram = new Histogram(100000);
        Uniqueness uniqueness = new Uniqueness();
        boolean uniqueValues;

        void recordValue(String value) {
            if (randomSample != null) {
                randomSample.recordValue(value);
            }
            if (histogram != null) {
                histogram.recordValue(value);
                if (histogram.isStorageOverflow()) {
                    histogram = null;
                }
            }
            if (uniqueness != null) {
                if (uniqueness.isRepeated(value)) {
                    if (histogram != null && histogram.getSize() == 1) {
                        throw new RuntimeException("wow"); // todo
                    }
                    uniqueness = null;
                }
            }
        }

        public void finish() {
            if (uniqueness != null) {
                uniqueValues = true;
                uniqueness = null;
                histogram = null;
            }
            if (histogram != null) {
                randomSample = null;
            }
        }

        public String toHtml() {
            StringBuilder html = new StringBuilder(String.format("<html><h3>Path: %s</h3>", path));
            if (uniqueValues) {
                html.append(String.format("<p>%d unique values, here are some random samples:</p><br>", total));
                html.append("<p>Random samples:</p><br><ul>");
                for (String value : randomSample.getValues()) {
                    html.append(String.format("<li><strong>%s</strong></li>", value));
                }
                html.append("</ul>");
            }
            else if (histogram != null) {
                if (histogram.getSize() == 1) {
                    Histogram.Counter counter = histogram.getCounters().iterator().next();
                    html.append(String.format("<p>A single value '%s' apppearing %d times.</p>", counter.getValue(), counter.getCount()));
                }
                else {
                    html.append(String.format("<p>%d different values, in descending order of frequency.</p><br>", histogram.getSize()));
                    List<Histogram.Counter> counterList = new ArrayList<Histogram.Counter>(histogram.getCounters());
                    Collections.sort(counterList);
                    html.append("<ul>");
                    for (Histogram.Counter counter : counterList) {
                        html.append(String.format("<li><strong>%s</strong>: %d occurrences or %s</li>", counter.getValue(), counter.getCount(), counter.getPercentage()));
                    }
                    html.append("</ul>");
                }
            }
            else {
                html.append(String.format("<p>%d values, too large a list to mantain, so here are some random samples:</p><br>", total));
                html.append("<ul>");
                for (String value : randomSample.getValues()) {
                    html.append(String.format("<li><strong>%s</strong></li>", value));
                }
                html.append("</ul>");
            }
            html.append("</html>");
            return html.toString();
        }
    }
}
