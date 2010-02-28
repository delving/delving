package eu.europeana.sip.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class YearExtractor implements Converter {
    private static final Pattern PATTERN = Pattern.compile("\\d{4}");

    @Override
    public String convertValue(String value) {
        Matcher matcher = PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group();
        }
        return "0000";
    }
}
