package eu.europeana.definitions.presentation;

import java.util.Date;

/**
 * todo: note that this is a copy of eu.europeana.core.querymodel.query.*
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 20, 2010 8:40:07 PM
 */

public interface DocId {
    String getEuropeanaUri();

    Date getTimestamp();
}
