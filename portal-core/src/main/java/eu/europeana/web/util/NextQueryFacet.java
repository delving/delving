package eu.europeana.web.util;

import eu.europeana.query.Facet;
import eu.europeana.query.FacetCount;
import eu.europeana.query.FacetType;
import eu.europeana.query.QueryModel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NextQueryFacet {
    private FacetType type;
    private List<FacetCountLink> links = new ArrayList<FacetCountLink>();

    public static List<NextQueryFacet> createDecoratedFacets(List<Facet> facets, QueryConstraints queryConstraints) throws UnsupportedEncodingException {
        List<NextQueryFacet> list = new ArrayList<NextQueryFacet>();
        for (Facet facet : facets) {
            list.add(new NextQueryFacet(facet, queryConstraints, false));
        }
        return list;
    }

    public static List<NextQueryFacet> createRemoveFacets(List<Facet> facets, QueryConstraints queryConstraints) throws UnsupportedEncodingException {
        List<NextQueryFacet> list = new ArrayList<NextQueryFacet>();
        for (Facet facet : facets) {
            list.add(new NextQueryFacet(facet, queryConstraints, true));
        }
        return list;
    }

    private NextQueryFacet(Facet facet, QueryConstraints queryConstraints, boolean onlyRemove) throws UnsupportedEncodingException {
        this.type = facet.getType();
        for (FacetCount count : facet.getCounts()) {
            boolean remove = false;
            StringBuilder url = new StringBuilder();
            for (QueryModel.Constraints.Entry entry : queryConstraints.getEntries()) {
                if (entry.getFacetType() == facet.getType()) {
                    if (count.getValue().equalsIgnoreCase(entry.getValue())) {
                        remove = true;
                    }
                    else {
                        QueryConstraints.appendToURI(url, entry.getFacetType().toString(), entry.getValue());
                    }
                }
                else {
                    QueryConstraints.appendToURI(url, entry.getFacetType().toString(), entry.getValue());
                }
            }
            if (onlyRemove) {
                if (remove) {
                    links.add(new FacetCountLink(count, url.toString(), true));
                }
            }
            else {
                if (!remove) {
                    QueryConstraints.appendToURI(url, facet.getType().toString(), count.getValue());
                }
                links.add(new FacetCountLink(count, url.toString(), remove));
            }
        }
    }

    public FacetType getType() {
        return type;
    }

    public List<FacetCountLink> getLinks() {
        return links;
    }

    public class FacetCountLink implements FacetCount {
        private FacetCount facetCount;
        private String url;
        private boolean remove;

        public FacetCountLink(FacetCount facetCount, String url, boolean remove) {
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
            return facetCount.getValue();
        }

        public Integer getCount() {
            return facetCount.getCount();
        }

        public String toString() {
            return "<a href='"+url+"'>"+facetCount.getValue()+"</a> "+(remove?"(remove)":"(add)");
        }
    }
}