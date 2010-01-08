package eu.europeana.json;

import eu.europeana.beans.AnnotationProcessor;
import eu.europeana.beans.EuropeanaBean;
import eu.europeana.beans.EuropeanaField;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import eu.europeana.query.QueryProblem;
import org.apache.commons.httpclient.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Set;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SolrQueryModelFactory implements QueryModelFactory {
    private HttpClient httpClient;
    private String baseUrl;

    @Value("#{europeanaProperties['solr.selectUrl']}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public QueryModel createQueryModel() {
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setSolrBaseUrl(baseUrl);
        queryModel.setHttpClient(httpClient);
        return queryModel;
    }

    // new solrJ implementation methods
    @Autowired
    private CommonsHttpSolrServer solrServer;

    public void setSolrServer(CommonsHttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }


    @Autowired
    private AnnotationProcessor annotationProcessor;

    // create solr query from http query parameters
    @Override
    public SolrQuery createFromQueryParams(Map<String, String[]> params) throws EuropeanaQueryException {
        SolrQuery solrQuery = new SolrQuery();
        if (!params.containsKey("query")) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString());
        }
        solrQuery.setQuery(params.get("query")[0]); // only get the first one
        if (params.containsKey("start")) {
            solrQuery.setStart(Integer.valueOf(params.get("start")[0]));
        }
        if (params.containsKey("rows")) {
            solrQuery.setRows(Integer.valueOf(params.get("rows")[0]));
        }
        solrQuery.setQueryType(findSolrQueryType(solrQuery.getQuery()));

        //set constraints
        solrQuery.setFilterQueries(params.get("qf"));


        return solrQuery;
    }

    private String findSolrQueryType(String query) {
        // todo: finish this
        QueryType queryType = null;

        return queryType.appearance;
    }

    @Override
    public QueryResponse getSolrResponse(SolrQuery solrQuery, Class<?> beanClass) throws EuropeanaQueryException { // add bean to ???
        // set facets
        Set<? extends EuropeanaField> facetFields = annotationProcessor.getFacetFields();

        // set search fields
        EuropeanaBean bean = annotationProcessor.getEuropeanaBean(beanClass);
        Set<EuropeanaField> fieldSet = bean.getFields();
        String[] fieldStrings = new String[fieldSet.size()];
        int index = 0;
        for (EuropeanaField europeanaField : fieldSet) {
            fieldStrings[index] = europeanaField.getFieldNameString();
            index++;
        }
        solrQuery.setFields(fieldStrings);


        //set more like this


        QueryResponse queryResponse;
        try {
            queryResponse = solrServer.query(solrQuery);
        } catch (SolrServerException e) {
//            log.error("Unable to fetch result", e);
                throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), e);
        }
        return queryResponse;
    }

    public enum QueryType {
        SIMPLE_QUERY("europeana"),
        ADVANCED_QUERY("standard"),
        MORE_LIKE_THIS_QUERY("moreLikeThis");

        private String appearance;

        QueryType(String appearance) {
            this.appearance = appearance;
        }

        public String toString() {
            return appearance;
        }
    }

}