package eu.europeana.database.domain;

import java.io.Serializable;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 2, 2009: 5:02:10 PM
 */
public interface QueueEntry extends Serializable {

    Long getId();

    EuropeanaCollection getCollection();

    Integer getRecordsProcessed();

    Integer getTotalRecords();

    boolean isCache();
}
