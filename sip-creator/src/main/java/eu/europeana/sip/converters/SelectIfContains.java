package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */


public class SelectIfContains implements Converter {
    private String selector;

    public SelectIfContains(String selector) {
        this.selector = selector;
    }

    @Override
    public String convertValue(String value) throws ConverterException {
        if (value.contains(selector)) {
            return value;
        }
        else {
            return "";
        }
    }
}