package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class PrefixSuffixSelectDelimitedDefault implements Converter {
    private String prefix, suffix, delimiter, selector, defaultValue;

    public PrefixSuffixSelectDelimitedDefault(String prefix, String suffix, String delimiter, String selector, String defaultValue) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.delimiter = delimiter;
        this.selector = selector;
        this.defaultValue = defaultValue;
    }

    @Override
    public String convertValue(String value) throws ConverterException {
        String [] parts = value.split(delimiter);
        for (String part : parts) {
            if (part.matches(selector)) {
                return prefix+part+suffix;
            }
        }
        return prefix+defaultValue+suffix;
    }
}