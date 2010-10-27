package eu.delving.core.util;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Override the message source so as to never return null values
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SafeMessageSource extends ReloadableResourceBundleMessageSource {

    /**
     * Resolve the code, never giving a null value back
     */
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        String result = super.resolveCodeWithoutArguments(code, locale);
        if (result == null) {
            result = code;
        }
        return result;
    }

    /**
     * Resolve the code, never giving a null value back
     */

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        MessageFormat result = super.resolveCode(code, locale);
        if (result == null) {
            result = new MessageFormat(code, locale);
        }
        return result;
    }
}
