package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class PrefixSuffixDelimited implements Converter {
    private static final String PIPE = "|";
    private String prefix, suffix, delimiter;

    public PrefixSuffixDelimited(String prefix, String suffix, String delimiter) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.delimiter = delimiter;
    }

    @Override
    public String convertValue(String value) {
        String [] parts = value.split(delimiter);
        StringBuilder whole = new StringBuilder();
        int pipes = parts.length-1;
        for (String part : parts) {
            whole.append(prefix).append(part).append(suffix);
            if (pipes > 0) {
                whole.append(PIPE);
                pipes--;
            }
        }
        return whole.toString();
    }
}