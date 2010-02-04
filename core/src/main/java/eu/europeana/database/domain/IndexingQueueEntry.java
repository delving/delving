package eu.europeana.database.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "indexqueue")
public class IndexingQueueEntry implements QueueEntry {
    private static final long serialVersionUID = 3194890599784829178L;

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
    private Date created;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    public IndexingQueueEntry(EuropeanaCollection collection) {
        this.collection = collection;
    }

    public IndexingQueueEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EuropeanaCollection getCollection() {
        return collection;
    }

    public void setCollection(EuropeanaCollection collection) {
        this.collection = collection;
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
        return false;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        return "IndexingQueueEntry{" +
                "collection=" + collection +
                ", recordsProcessed=" + recordsProcessed +
                ", totalRecords=" + totalRecords +
                "}";
    }
}