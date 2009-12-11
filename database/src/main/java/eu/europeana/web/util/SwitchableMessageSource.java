package eu.europeana.web.util;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

/**
 * Switch between multiple sources
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SwitchableMessageSource implements MessageSource {
    private Logger log = Logger.getLogger(getClass());
    private Map<String, MessageSource> sources;
    private String choice;
    private MessageSource sourceInstance;

    public void setSources(Map<String, MessageSource> sources) {
        this.sources = sources;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    private MessageSource source() {
        if (sourceInstance == null) {
            sourceInstance = sources.get(choice);
            if (sourceInstance == null) {
                throw new IllegalArgumentException("Choice must be one of the map keys "+sources.keySet()+", but it's ["+choice+"]");
            }
            log.info("MessageSource chosen to be '"+choice+"'");
        }
        return sourceInstance;
    }

    public String getMessage(String s, Object[] objects, String s1, Locale locale) {
        return source().getMessage(s, objects, s1, locale);
    }

    public String getMessage(String s, Object[] objects, Locale locale) throws NoSuchMessageException {
        return source().getMessage(s, objects, locale);
    }

    public String getMessage(MessageSourceResolvable messageSourceResolvable, Locale locale) throws NoSuchMessageException {
        return source().getMessage(messageSourceResolvable, locale);
    }
}
