package eu.europeana.core.querymodel.query;

import org.apache.solr.client.solrj.SolrQuery;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 20, 2010 5:41:56 PM
 */
public class SolrQueryUtil {


    public static String[] getFilterQueriesAsPhrases(SolrQuery solrQuery) {
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null) {
            return null;
        }
        List<String> phraseFilterQueries = new ArrayList<String>(filterQueries.length);
        for (String facetTerm : filterQueries) {
            int colon = facetTerm.indexOf(":");
            String facetName = facetTerm.substring(0, colon);
            String facetValue = facetTerm.substring(colon + 1);
            phraseFilterQueries.add(MessageFormat.format("{0}:\"{1}\"", facetName, facetValue));
        }
        return phraseFilterQueries.toArray(new String[phraseFilterQueries.size()]);
    }

    public static String[] getFilterQueriesWithoutPhrases(SolrQuery solrQuery) {
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null) {
            return null;
        }
        List<String> nonPhraseFilterQueries = new ArrayList<String>(filterQueries.length);
        for (String facetTerm : filterQueries) {
            int colon = facetTerm.indexOf(":");
            String facetName = facetTerm.substring(0, colon);
            if (facetName.contains("!tag")) {
                facetName = facetName.replaceFirst("\\{!tag=.*?\\}", "");
            }
            String facetValue = facetTerm.substring(colon + 1);
            if (facetValue.length() >= 2 && facetValue.startsWith("\"") && facetValue.endsWith("\"")) {
                facetValue = facetValue.substring(1, facetValue.length() - 1);
            }
            nonPhraseFilterQueries.add(facetName+":"+facetValue);
        }
        return nonPhraseFilterQueries.toArray(new String[nonPhraseFilterQueries.size()]);
    }

     /*
     * Transform "LANGUAGE:en, LANGUAGE:de, PROVIDER:"The European Library" "  to "{!tag=lang}LANGUAGE:(en OR de), PRODIVER:"The European Library" "
     */

    public static String[] getFilterQueriesAsOrQueries(SolrQuery solrQuery, Map<String, String> facetMap) {
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null) {
            return null;
        }
        String[] sortedFilterQueries = filterQueries.clone();
        Arrays.sort(sortedFilterQueries);
        Map<String, List<String>> terms = new TreeMap<String, List<String>>();
        for (String facetTerm : sortedFilterQueries) {
            int colon = facetTerm.indexOf(":");
            String facetName = facetTerm.substring(0, colon);
            if (facetMap.containsKey(facetName)) {
                String facetPrefix = facetMap.get(facetName);
                facetName = String.format("{!tag=%s}%s", facetPrefix, facetName);
            }
            String facetValue = facetTerm.substring(colon + 1);
            List<String> values = terms.get(facetName);
            if (values == null) {
                terms.put(facetName, values = new ArrayList<String>());
            }
            values.add(facetValue);
        }
        List<String> queries = new ArrayList<String>(sortedFilterQueries.length);
        for (Map.Entry<String, List<String>> entry : terms.entrySet()) {
            String facetName = entry.getKey();
            String facetValue;
            if (entry.getValue().size() == 1) {
//                facetValue = '"' + entry.getValue().get(0) + '"';
                facetValue = entry.getValue().get(0);
            }
            else {
                StringBuilder orStatement = new StringBuilder("(");
                Iterator<String> walk = entry.getValue().iterator();
                while (walk.hasNext()) {
                    String value = walk.next();
//                    orStatement.append('"').append(value).append('"');
                    orStatement.append(value);
                    if (walk.hasNext()) {
                        orStatement.append(" OR ");
                    }
                }
                orStatement.append(")");
                facetValue = orStatement.toString();
            }
            queries.add(facetName+":"+facetValue);
        }
        return queries.toArray(new String[queries.size()]);
    }
}
