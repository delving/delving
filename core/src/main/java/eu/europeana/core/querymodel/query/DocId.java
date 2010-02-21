package eu.europeana.core.querymodel.query;

import java.util.Date;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 20, 2010 8:40:07 PM
 */
public interface DocId {
    String getEuropeanaUri();

    Date getTimestamp();
}
