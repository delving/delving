package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class PrefixSuffixFirstDelimited implements Converter {
    private String prefix, suffix, delimiter;

    public PrefixSuffixFirstDelimited(String prefix, String suffix, String delimiter) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.delimiter = delimiter;
    }

    @Override
    public String convertValue(String value) {
        String [] parts = value.split(delimiter);
        return prefix+parts[0]+suffix;
    }
}