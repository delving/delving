package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Encapsulates both types of queue entries for client-server transport
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class QueueEntryX implements IsSerializable {
    private long id;
    private Type type;
    private EuropeanaCollectionX collection;
    private int recordsProcessed;
    private int totalRecords;

    public QueueEntryX(long id, Type type, EuropeanaCollectionX collection, int recordsProcessed, int totalRecords) {
        this.id = id;
        this.type = type;
        this.collection = collection;
        this.recordsProcessed = recordsProcessed;
        this.totalRecords = totalRecords;
    }

    public QueueEntryX() {
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
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

    public enum Type implements IsSerializable {
        INDEX,
        CACHE
    }

    public String toString() {
        return "QueueEntry("+type+", "+collection.getName()+", processed="+recordsProcessed+", total="+totalRecords+")";
    }
}
