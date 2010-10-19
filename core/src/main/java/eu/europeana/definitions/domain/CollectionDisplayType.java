package eu.europeana.definitions.domain;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10/13/10 12:09 PM
 */
public enum CollectionDisplayType {
    ALL("all"),
    PORTAL("portal"),
    MUSEOMETRIE("mm");

    private String solrSearchName;

    CollectionDisplayType(String solrSearchName) {
        this.solrSearchName = solrSearchName;
    }

    public String getSolrSearchName() {
        return solrSearchName;
    }
}
