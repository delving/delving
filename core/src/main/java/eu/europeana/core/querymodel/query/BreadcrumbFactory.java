package eu.europeana.core.querymodel.query;

import eu.delving.core.util.ThemeInterceptor;
import org.apache.solr.client.solrj.SolrQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Build a list of breadcrumbs
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class BreadcrumbFactory {
    private static final String FACET_PROMPT = "&amp;qf=";

    public List<Breadcrumb> createList(SolrQuery solrQuery, Locale locale) throws EuropeanaQueryException {
        if (solrQuery.getQuery() == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString());
        }
        List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
        String prefix = "query=" + encode(solrQuery.getQuery());
        String translatedQuery = sanitizeAndTranslate(solrQuery.getQuery(), locale);
        breadcrumbs.add(new Breadcrumb(prefix, translatedQuery, "", solrQuery.getQuery()));
        if (solrQuery.getFilterQueries() != null) {
            int facetQueryCount = solrQuery.getFilterQueries().length;
            for (int walk = 0; walk < facetQueryCount; walk++) {
                StringBuilder out = new StringBuilder(prefix);
                int count = walk;
                for (String facetTerm : SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery)) {
                    if (facetTerm.contains(":")) {
                        int colon = facetTerm.indexOf(":");
                        String facetName = facetTerm.substring(0, colon);
                        String facetValue = facetTerm.substring(colon + 1);
                        appendToURI(out, facetName, facetValue);
                        if (count-- == 0) {
                            String translatedFacetName = ThemeInterceptor.getLookup().toLocalizedName(facetName, locale);
                            breadcrumbs.add(new Breadcrumb(out.toString(), translatedFacetName + ":" + facetValue, facetName, facetValue));
                            break;
                        }
                    }
                }
            }
        }
        breadcrumbs.get(breadcrumbs.size() - 1).flagAsLast();
        return breadcrumbs;

    }

    private List<String> extractParts(String query) {
        List<String> answer = new ArrayList<String>();
        String[] queryParts = query.split("\\s+");
        for (String part : queryParts) {
            if (BOOLEAN_KEYWORDS.contains(part)) {
                part = part.toUpperCase();
            }
            StringBuilder out = new StringBuilder();
            for (int walk = 0; walk < part.length(); walk++) {
                char ch = part.charAt(walk);
                switch (ch) {
                    case '{':
                    case '}':
                        break;
                    default:
                        out.append(ch);
                }
            }
            if (out.length() > 0) {
                answer.add(out.toString());
            }
        }
        return answer;
    }

    public String sanitizeAndTranslate(String query, Locale locale) {
        StringBuilder out = new StringBuilder();
        for (String part : extractParts(query)) {
            int colon = part.indexOf(":");
            if (colon < 0) {
                out.append(part).append(" ");
            }
            else {
                String facet = part.substring(0, colon);
                String value = part.substring(colon+1);
                facet = ThemeInterceptor.getLookup().toLocalizedName(facet, locale);
                out.append(facet).append(":").append(value).append(" ");
            }
        }
        return out.toString().trim();
    }

    private static void appendToURI(StringBuilder uri, String name, String value) {
        uri.append(FACET_PROMPT).append(name).append(":").append(encode(value));
    }

    private static String encode(String value) {
        if (value == null) {
            throw new RuntimeException("Cannot encode null value!");
        }
        try {
            return URLEncoder.encode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Set<String> BOOLEAN_KEYWORDS = new TreeSet<String>();

    private static void addBooleanKeyword(String keyword) {
        BOOLEAN_KEYWORDS.add(keyword);
        BOOLEAN_KEYWORDS.add(keyword.toLowerCase());
    }

    static {
        addBooleanKeyword("AND");
        addBooleanKeyword("OR");
        addBooleanKeyword("NOT");
    }

}
