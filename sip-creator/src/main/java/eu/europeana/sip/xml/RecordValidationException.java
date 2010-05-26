package eu.europeana.sip.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Gather some validation problems together into an exception that can be thrown
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordValidationException extends Exception {
    private List<String> problems = new ArrayList<String>();

    public RecordValidationException(List<String> problems) {
        super(problems.size() + " Record Validation Problems");
        this.problems = problems;
    }

    public List<String> getProblems() {
        return problems;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("Problems:\n");
        for (String problem : problems) {
            out.append(problem).append('\n');
        }
        return out.toString();
    }
}
