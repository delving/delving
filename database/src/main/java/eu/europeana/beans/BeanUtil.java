package eu.europeana.beans;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class BeanUtil {
    private static final String[] STRINGS = new String[]{};
    private static final String EMPTY_STRING = " ";

    static String returnStringOrElse(String[] s) {
        return (s != null) ? s[0] : EMPTY_STRING;
    }

    static String returnStringOrElse(String s) {
        return (s != null) ? s : EMPTY_STRING;
    }

    static String[] returnArrayOrElse(String[] s) {
        return (s != null) ? s : STRINGS;
    }

    static String[] returnArrayOrElse (String[] ... arrs) {
        for (String[] arr : arrs) {
            if (arr != null) {
                return arr;
            }
        }
        return STRINGS;
    }


}