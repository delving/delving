package eu.europeana.sip.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */


public class Replace implements Converter {
    private String from, to;

    public Replace(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String convertValue(String value) {
        return value.replace(from, to);
    }
}