package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class Delimiter implements Converter {
    private static final String PIPE = "|";
    private String delimiter;

    public Delimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String convertValue(String value) {
        String [] parts = value.split(delimiter);
        StringBuilder whole = new StringBuilder();
        int pipes = parts.length-1;
        for (String part : parts) {
            whole.append(part);
            if (pipes > 0) {
                whole.append(PIPE);
                pipes--;
            }
        }
        return whole.toString();
    }
}