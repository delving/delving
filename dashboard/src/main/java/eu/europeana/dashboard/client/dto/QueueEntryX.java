package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Encapsulates both types of queue entries for client-server transport
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class QueueEntryX implements IsSerializable {
    private long id;
    private EuropeanaCollectionX collection;
    private int recordsProcessed;
    private int totalRecords;

    public QueueEntryX(long id, EuropeanaCollectionX collection, int recordsProcessed, int totalRecords) {
        this.id = id;
        this.collection = collection;
        this.recordsProcessed = recordsProcessed;
        this.totalRecords = totalRecords;
    }

    public QueueEntryX() {
    }

    public long getId() {
        return id;
    }

    public EuropeanaCollectionX getCollection() {
        return collection;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public String toString() {
        return "QueueEntry("+collection.getName()+", processed="+recordsProcessed+", total="+totalRecords+")";
    }
}
