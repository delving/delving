package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * This is to mirror the EuropeanaId class
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EuropeanaIdX implements IsSerializable {
    private Long id;
    private Integer timesViewed;
    private Date created;
    private Date lastViewed;
    private Date lastModified;
    private String europeanaUri;
    private Float boostFactor;
    private String solrRecords;

    public EuropeanaIdX(Long id, Integer timesViewed, Date created, Date lastViewed, Date lastModified, String europeanaUri, Float boostFactor, String solrRecords) {
        this.id = id;
        this.timesViewed = timesViewed;
        this.created = created;
        this.lastViewed = lastViewed;
        this.lastModified = lastModified;
        this.europeanaUri = europeanaUri;
        this.boostFactor = boostFactor;
        this.solrRecords = solrRecords;
    }

    public EuropeanaIdX() {
    }

    public Long getId() {
        return id;
    }

    public Integer getTimesViewed() {
        return timesViewed;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastViewed() {
        return lastViewed;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public Float getBoostFactor() {
        return boostFactor;
    }

    public String getSolrRecords() {
        return solrRecords;
    }

    public void setBoostFactor(Float boostFactor) {
        this.boostFactor = boostFactor;
    }

    public void setSolrRecords(String solrRecords) {
        this.solrRecords = solrRecords;
    }
}
