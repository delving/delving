package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class PrefixSuffixReplace implements Converter {
    private String prefix, suffix, from, to;

    public PrefixSuffixReplace(String prefix, String suffix, String from, String to) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.from = from;
        this.to = to;
    }

    @Override
    public String convertValue(String value) {
        String embellished = prefix+value+suffix;
        return embellished.replace(from, to);
    }
}