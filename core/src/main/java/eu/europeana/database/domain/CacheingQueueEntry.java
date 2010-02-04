package eu.europeana.database.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cachequeue")
public class CacheingQueueEntry implements QueueEntry {
    private static final long serialVersionUID = 4571763703752911930L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collectionid", updatable = true, nullable = false)
    private EuropeanaCollection collection;

    @Column(nullable = true)
    private Long lastProcessedRecordId;

    @Column
    private Integer recordsProcessed;

    @Column
    private Integer totalRecords;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    public CacheingQueueEntry(EuropeanaCollection collection) {
        this.collection = collection;
    }

    public CacheingQueueEntry() {
    }

    public Long getId() {
        return id;
    }

    public EuropeanaCollection getCollection() {
        return collection;
    }

    public Long getLastProcessedRecordId() {
        return lastProcessedRecordId;
    }

    public void setLastProcessedRecordId(Long lastProcessedRecordId) {
        this.lastProcessedRecordId = lastProcessedRecordId;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public boolean isCache() {
        return true;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "CacheingQueueEntry{" +
                "collection=" + collection +
                ", recordsProcessed=" + recordsProcessed +
                ", totalRecords=" + totalRecords +
                '}';
    }
}