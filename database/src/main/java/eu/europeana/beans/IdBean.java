package eu.europeana.beans;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:15:43 AM
 */

@EuropeanaView(facets = false, rows = 10)
public class IdBean {

    @Europeana(briefDoc = true)
    @Solr(namespace = "europeana", name = "uri", multivalued = false, required = true)
    @Field("europeana_uri")
    String europeanaUri;

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public String getId() {
        return europeanaUri;
    }
}
