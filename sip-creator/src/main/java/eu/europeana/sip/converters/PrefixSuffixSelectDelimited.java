package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class PrefixSuffixSelectDelimited implements Converter {
    private String prefix, suffix, delimiter, selector;

    public PrefixSuffixSelectDelimited(String prefix, String suffix, String delimiter, String selector) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.delimiter = delimiter;
        this.selector = selector;
    }

    @Override
    public String convertValue(String value) throws ConverterException {
        String [] parts = value.split(delimiter);
        for (String part : parts) {
            if (part.matches(selector)) {
                return prefix+part+suffix;
            }
        }
        throw new ConverterException("Value ["+value+"] does not contain selector ["+selector+"]");
    }
}