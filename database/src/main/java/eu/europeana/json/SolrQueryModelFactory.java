package eu.europeana.json;

<<<<<<< HEAD
import eu.europeana.beans.AnnotationProcessor;
import eu.europeana.query.*;
import eu.europeana.web.util.NextQueryFacet;
import eu.europeana.web.util.QueryConstraints;
import eu.europeana.web.util.ResultPaginationImpl;
=======
import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
>>>>>>> #545 Created NewQueryModelFactory interface for new bean stuff to keep it separate from old QueryModel and ResultModel approach
import org.apache.commons.httpclient.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

<<<<<<< HEAD
import java.util.List;
import java.util.Map;

=======
>>>>>>> #545 Created NewQueryModelFactory interface for new bean stuff to keep it separate from old QueryModel and ResultModel approach
/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Deprecated
public class SolrQueryModelFactory implements QueryModelFactory {
<<<<<<< HEAD

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
        QueryType queryType = QueryType.SIMPLE_QUERY;

        return queryType.appearance;
    }

    private eu.europeana.query.ResultPagination createPagination(QueryResponse response, SolrQuery query) {
        int numFound = Integer.parseInt(response.getResponseHeader().get("numFound").toString());
        return new ResultPaginationImpl(numFound, query.getStart(), query.getRows());
    }

    // I am not quite sure this is the right place

    private List<QueryConstraints.Breadcrumb> createBreadcrumbs() {
        return null;
    }

    private Class<?> briefBean;

    public void setBriefBean(Class<?> briefBean) {
        this.briefBean = briefBean;
    }

    @Override
    public BriefBeanView getBriefResultView(SolrQuery solrQuery) throws EuropeanaQueryException {
        return new BriefBeanViewImpl(solrQuery, getSolrResponse(solrQuery, briefBean));
    }


    public interface BriefBeanView {
        List<? extends BriefDoc> getBriefDocs();
        List<NextQueryFacet> getNextQueryFacets();
        ResultPagination getPagination();
    }

    public class BriefBeanViewImpl implements BriefBeanView {
        private SolrQuery solrQuery;
        private QueryResponse solrResponse;
        private ResultPagination pagination;

        private BriefBeanViewImpl(SolrQuery solrQuery, QueryResponse solrResponse) {
            this.solrQuery = solrQuery;
            this.solrResponse = solrResponse;
            pagination = createPagination(solrResponse, solrQuery);
        }

        @Override
        public List<? extends BriefDoc> getBriefDocs() {
            return (List<BriefDoc>) solrResponse.getBeans(briefBean);
        }

        @Override
        public List<NextQueryFacet> getNextQueryFacets() {
            return null;  //TODO: implement this
        }

        @Override
        public ResultPagination getPagination() {
            return pagination;
        }
    }

    @Override
    public QueryResponse getSolrResponse(SolrQuery solrQuery, Class<?> beanClass) throws EuropeanaQueryException { // add bean to ???
        // set facets
//        Set<? extends EuropeanaField> facetFields = annotationProcessor.getFacetFields();

        // set search fields
//        EuropeanaBean bean = annotationProcessor.getEuropeanaBean(beanClass);
//        Set<EuropeanaField> fieldSet = bean.getFields();
//        String[] fieldStrings = new String[fieldSet.size()];
//        int index = 0;
//        for (EuropeanaField europeanaField : fieldSet) {
//            fieldStrings[index] = europeanaField.getFieldNameString();
//            index++;
//        }
//        solrQuery.setFields(fieldStrings);


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

    // todo remove later

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


=======

    // todo remove later

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

    @Deprecated
    public QueryModel createQueryModel() {
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setSolrBaseUrl(baseUrl);
        queryModel.setHttpClient(httpClient);
        return queryModel;
    }

>>>>>>> #545 Created NewQueryModelFactory interface for new bean stuff to keep it separate from old QueryModel and ResultModel approach
}