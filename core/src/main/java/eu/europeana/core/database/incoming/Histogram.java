package eu.europeana.core.database.incoming;

import java.text.DecimalFormat;

/**
 * maintain a histogram of performance, using fibonacci sequence for geometrical sizing
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Histogram {
    private static final DecimalFormat MILLIS_FORMAT = new DecimalFormat("000000");
    private static final DecimalFormat COUNT_FORMAT = new DecimalFormat("0000000");
    private final long STEP_MILLIS = 10L;
    private String name;
    private int[] countArray = new int[20];

    public Histogram(String name) {
        this.name = name;
    }

    public void recordDuration(long duration) {
        int tenths = (int) (duration / STEP_MILLIS);
        int which = 0;
        int prev = 1;
        int fib = 1;
        while (tenths >= fib && which < countArray.length - 1) {
            int save = fib;
            fib += prev;
            prev = save;
            which++;
        }
        countArray[which]++;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("Histogram (" + name + ") {\n");
        int prev = 0;
        int fib = 1;
        for (int count : countArray) {
            int save = fib;
            fib += prev;
            prev = save;
            out.append('\t');
            out.append(MILLIS_FORMAT.format(fib * STEP_MILLIS)).append(": ");
            out.append(COUNT_FORMAT.format(count)).append("\n");
        }
        out.append("}\n");
        return out.toString();
    }
}
