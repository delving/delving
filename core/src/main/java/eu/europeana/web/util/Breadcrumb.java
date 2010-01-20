package eu.europeana.web.util;

import org.apache.solr.client.solrj.SolrQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Track back to where you came from
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class Breadcrumb {
    private static final String FACET_PROMPT = "&qf=";
    private String href;
    private String display;
    private boolean last;

    public Breadcrumb(String href, String display) {
        this.href = href;
        this.display = display;
    }

    public void flagAsLast() {
        this.last = true;
    }

    public String getDisplay() {
        return display;
    }

    public String getHref() {
        return href;
    }

    public boolean getLast() {
        return last;
    }

    public String toString() {
        return "<a href=\"" + href + "\">" + display + "</a>";
    }

    public static List<Breadcrumb> createList(SolrQuery solrQuery) {
        List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
        String prefix = "query=" + encode(solrQuery.getQuery());
        breadcrumbs.add(new Breadcrumb(prefix, solrQuery.getQuery()));
        if (solrQuery.getFilterQueries() != null) {
            int facetQueryCount = solrQuery.getFilterQueries().length;
            for (int walk = 0; walk < facetQueryCount; walk++) {
                StringBuilder out = new StringBuilder(prefix);
                int count = walk;
                for (String facetTerm : solrQuery.getFilterQueries()) {
                    int colon = facetTerm.indexOf(":");
                    String facetName = facetTerm.substring(0, colon);
                    String facetValue = facetTerm.substring(colon + 1);
                    appendToURI(out, facetName, facetValue);
                    if (count-- == 0) {
                        breadcrumbs.add(new Breadcrumb(out.toString(), facetName + ":" + encode(facetValue)));
                        break;
                    }
                }
            }
        }
        breadcrumbs.get(breadcrumbs.size() - 1).flagAsLast();
        return breadcrumbs;

    }

    private static void appendToURI(StringBuilder uri, String name, String value) {
        uri.append(FACET_PROMPT).append(name).append(":").append(encode(value));
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
