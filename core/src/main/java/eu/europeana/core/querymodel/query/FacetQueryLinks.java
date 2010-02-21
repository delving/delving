package eu.europeana.core.querymodel.query;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Build a list of alternative queries involving adding or removing facets, based on the current
 * facets of the query and the counts returned from the query.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FacetQueryLinks {
    private static final String FACET_PROMPT = "&qf=";
    private String type;
    private boolean facetSelected = false;
    private List<FacetCountLink> links = new ArrayList<FacetCountLink>();

    public static List<FacetQueryLinks> createDecoratedFacets(SolrQuery solrQuery, List<FacetField> facetFields) throws UnsupportedEncodingException {
        List<FacetQueryLinks> list = new ArrayList<FacetQueryLinks>();
        for (FacetField facetField : facetFields) {
            list.add(new FacetQueryLinks(facetField, solrQuery, false));
        }
        return list;
    }

    private FacetQueryLinks(FacetField facetField, SolrQuery solrQuery, boolean onlyRemove) throws UnsupportedEncodingException {
        this.type = facetField.getName();
        if (facetField.getValueCount() > 0) {
            for (FacetField.Count count : facetField.getValues()) {
                if (temporarilyPreventYear0000(this.type, count.getName())) {
                    continue;
                }
                boolean remove = false;
                StringBuilder url = new StringBuilder();
                if (solrQuery.getFilterQueries() != null) {
                    for (String facetTerm : SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery)) {
                        int colon = facetTerm.indexOf(":");
                        String facetName = facetTerm.substring(0, colon);
                        String facetValue = facetTerm.substring(colon + 1);
                        if (temporarilyPreventYear0000(facetName, facetValue)) {
                            continue;
                        }
                        if (facetName.equalsIgnoreCase(facetField.getName())) {
                            if (count.getName().equalsIgnoreCase(facetValue)) {
                                remove = true;
                                facetSelected = true;
                            }
                            else {
                                url.append(FACET_PROMPT).append(facetTerm);
                            }
                        }
                        else {
                            url.append(FACET_PROMPT).append(facetTerm);
                        }
                    }
                }
                if (onlyRemove) {
                    if (remove) {
                        links.add(new FacetCountLink(count, url.toString(), true));
                    }
                }
                else {
                    if (!remove) {
                        url.append(FACET_PROMPT).append(count.getFacetField().getName() + ":" + count.getName());
                    }
                    links.add(new FacetCountLink(count, url.toString(), remove));
                }
            }
        }
    }

    /**
     * At one point the normalizer was letting unparseable values of year, coded ast 0000,
     * through to the index.  This bandage solution prevents them from being presented.
     *
     * @param facetName year
     * @param facetValue 0000
     * @return true if it matches
     */

    private boolean temporarilyPreventYear0000(String facetName, String facetValue) {
        return "YEAR".equals(facetName) && "0000".equals(facetValue);
    }

    public String getType() {
        return type;
    }

    public List<FacetCountLink> getLinks() {
        return links;
    }

    public boolean isSelected() {
        return facetSelected;
    }

    public class FacetCountLink {
        private FacetField.Count facetCount;
        private String url;
        private boolean remove;

        public FacetCountLink(FacetField.Count facetCount, String url, boolean remove) {
            this.facetCount = facetCount;
            this.url = url;
            this.remove = remove;
        }

        public String getUrl() {
            return url;
        }

        public boolean isRemove() {
            return remove;
        }

        public String getValue() {
            return facetCount.getName();
        }

        public long getCount() {
            return facetCount.getCount();
        }

        public String toString() {
            return "<a href='" + url + "'>" + getValue() + "</a> " + (remove ? "(remove)" : "(add)");
        }
    }
}