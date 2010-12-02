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
import java.util.Set;

/**
 * Maintain a map of strings and counters
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Statistics implements Comparable<Statistics>, Serializable {
    private Path path;
    private int total;
    private ValueStats valueStats;
    private String lazyHtml;

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

    public Set<String> getHistogramValues() {
        if (valueStats == null || valueStats.histogram == null) return null;
        return valueStats.histogram.getValues();
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
            if (total == 1) {
                return String.format("<html><html><h3>Path: %s</h3><p>Element appears once.</p>", path);
            }
            else {
                return String.format("<html><html><h3>Path: %s</h3><p>Element appears %d times.</p>", path, total);
            }
        }
        else {
            return valueStats.toHtml();
        }
    }

    private class ValueStats implements Serializable {
        RandomSample randomSample = new RandomSample(200);
        Histogram histogram = new Histogram(600000, 500);
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
                    uniqueness = null;
                }
            }
        }

        public void finish() {
            if (uniqueness != null) {
                uniqueness = null;
                if (total > 1) {
                    uniqueValues = true;
                    histogram = null;
                }
            }
            if (histogram != null) {
                randomSample = null;
            }
        }

        public String toHtml() {
            if (lazyHtml == null) {
                StringBuilder html = new StringBuilder(String.format("<html><h3>Path: %s</h3>", path));
                if (uniqueValues) {
                    html.append(String.format("<p>All values are unique, and there are <strong>%d</strong>, so here are some random samples:</p>", total));
                    html.append("<ul>");
                    for (String value : randomSample.getValues()) {
                        html.append(String.format("<li>'<strong>%s</strong>'</li>", value));
                    }
                    html.append("</ul>");
                }
                else if (histogram != null) {
                    if (histogram.getSize() == 1) {
                        Histogram.Counter counter = histogram.getCounters().iterator().next();
                        html.append(String.format("<p>There is a single value '<strong>%s</strong>' apppearing <strong>%d</strong> times.</p>", counter.getValue(), counter.getCount()));
                    }
                    else {
                        html.append(String.format("<p>There are <strong>%d</strong> different values, in descending order of frequency.</p>", histogram.getSize()));
                        List<Histogram.Counter> counterList = new ArrayList<Histogram.Counter>(histogram.getCounters());
                        Collections.sort(counterList);
                        html.append("<table cellpadding=30px><tr><td><table cellpadding=4px>");
                        for (Histogram.Counter counter : counterList) {
                            html.append(String.format("<tr></td><td>%d</td><td>%s</td><td><strong>%s</strong></tr>", counter.getCount(), counter.getPercentage(), counter.getValue()));
                        }
                        html.append("</table></td></tr></table>");
                    }
                }
                else {
                    html.append(String.format("There are more than <p><strong>%d</strong> different values, too large a list to mantain, so here are some random samples:</p>", total));
                    html.append("<ul>");
                    for (String value : randomSample.getValues()) {
                        html.append(String.format("<li>'<strong>%s</strong>'</li>", value));
                    }
                    html.append("</ul>");
                }
                html.append("</html>");
                lazyHtml = html.toString();
            }
            return lazyHtml;
        }
    }
}
