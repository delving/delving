package eu.europeana.beans.query;

import eu.europeana.beans.*;
import eu.europeana.beans.views.BriefBeanView;
import eu.europeana.beans.views.FullBeanView;
import eu.europeana.beans.views.GridBrowseBeanView;
import eu.europeana.query.*;
import eu.europeana.web.util.DocIdWindowPagerImpl;
import eu.europeana.web.util.FacetQueryLinks;
import eu.europeana.web.util.ResultPaginationImpl;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class BeanQueryModelFactory implements NewQueryModelFactory {
    private CommonsHttpSolrServer solrServer;
    private AnnotationProcessor annotationProcessor;

    @Autowired
    public void setSolrServer(CommonsHttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Autowired
    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }
    
    /**
     * create solr query from http query parameters
     */
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

    @Override
    public SolrQuery createFromUri(String europeanaUri) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("europeana_uri:\""+europeanaUri+"\"");
        return solrQuery;
    }

    private Class<?> briefBean;

    public void setBriefBean(Class<?> briefBean) {
        this.briefBean = briefBean;
    }

    private Class<?> fullBean;

    public void setFullBean(Class<?> fullBean) {
        this.fullBean = fullBean;
    }

    private Class<?> idBean;

    public void setIdBean(Class<?> idBean) {
        this.idBean = idBean;
    }

    @Override
    public BriefBeanView getBriefResultView(SolrQuery solrQuery, String requestQueryString) throws EuropeanaQueryException, UnsupportedEncodingException {
        QueryResponse queryResponse = getSolrResponse(solrQuery, briefBean);
        return new BriefBeanViewImpl(solrQuery, queryResponse, requestQueryString);
    }

    @Override
    public FullBeanView getFullResultView(SolrQuery solrQuery, Map<String, String[]> params) throws EuropeanaQueryException {
        return new FullBeanViewImpl(solrQuery, getSolrResponse(solrQuery, fullBean), params);  //TODO: implement this
    }

    @Override
    public FullDoc getFullDoc(SolrQuery solrQuery) throws EuropeanaQueryException {
        QueryResponse response = getSolrResponse(solrQuery);
        List<FullBean> fullBeanList = response.getBeans(FullBean.class);
        if (fullBeanList.size() != 1) {
            throw new EuropeanaQueryException("Full Doc not found");
        }
        return fullBeanList.get(0);
    }

    @Override
    public GridBrowseBeanView getGridBrowseResultView(SolrQuery solrQuery) throws EuropeanaQueryException {
        return null;  //TODO: implement this
    }

    //todo review this code
    @Override
    public List<IdBean> getDocIdList(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException {
        SolrQuery solrQuery = createFromQueryParams(params);
        Integer start = solrQuery.getStart();
        if (start  > 1 ) {
            solrQuery.setStart(start - 2);
        }
        solrQuery.setRows(3);
        solrQuery.setFields("europeana_uri");
        // Fetch results from server
        QueryResponse queryResponse = solrServer.query(solrQuery);
        // fetch beans
        return queryResponse.getBeans(IdBean.class);
    }


    public class BriefBeanViewImpl implements BriefBeanView {
        private ResultPagination pagination;
        private List<? extends BriefDoc> briefDocs;
        private List<FacetQueryLinks> queryLinks;

        private BriefBeanViewImpl(SolrQuery solrQuery, QueryResponse solrResponse, String requestQueryString) throws UnsupportedEncodingException {
            pagination = createPagination(solrResponse, solrQuery, requestQueryString);
            briefDocs = (List<? extends BriefDoc>) solrResponse.getBeans(briefBean);
            queryLinks = FacetQueryLinks.createDecoratedFacets(solrQuery, solrResponse.getFacetFields());
        }

        @Override
        public List<? extends BriefDoc> getBriefDocs() {
            return briefDocs;
        }

        @Override
        public List<FacetQueryLinks> getQueryFacetsLinks() {
            return queryLinks;
        }

        @Override
        public ResultPagination getPagination() {
            return pagination;
        }
    }

    private class FullBeanViewImpl implements FullBeanView {
        private SolrQuery solrQuery;
        private QueryResponse solrResponse;
        private Map<String, String[]> params;

        private FullBeanViewImpl(SolrQuery solrQuery, QueryResponse solrResponse, Map<String, String[]> params) {
            this.solrQuery = solrQuery;
            this.solrResponse = solrResponse;
            this.params = params;
        }

        @Override
        public DocIdWindowPager getDocIdWindowPager() throws Exception {
            return DocIdWindowPagerImpl.fetchPager(params, solrQuery, solrServer);
        }

        @Override
        public List<? extends BriefDoc> getRelatedItems() {
            return solrResponse.getBeans(BriefBean.class);
        }

        @Override
        public FullDoc getFullDoc() {
            SolrDocumentList matchDoc = (SolrDocumentList) solrResponse.getResponse().get("match");
            List<FullBean> fullBean = solrServer.getBinder().getBeans(FullBean.class, matchDoc);
            return fullBean.get(0);
        }
    }

    @Override
    public QueryResponse getSolrResponse(SolrQuery solrQuery) throws EuropeanaQueryException {
        QueryResponse queryResponse;
        try {
            queryResponse = solrServer.query(solrQuery);
        } catch (SolrServerException e) {
//            log.error("Unable to fetch result", e);
            //todo determine which errors the SolrServer can throw
            throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), e);
        }
        return queryResponse;
    }

    @Override
    public QueryResponse getSolrResponse(SolrQuery solrQuery, Class<?> beanClass) throws EuropeanaQueryException { // add bean to ???
        // set facets
        if (beanClass == briefBean) {
            solrQuery.setFacet(true);
            solrQuery.setRows(12); // todo replace with annotation later
            solrQuery.addFacetField("LANGUAGE");
//            solrQuery.addFacetField(annotationProcessor.getFacetFieldStrings());
        }
        // set search fields
        EuropeanaBean bean = annotationProcessor.getEuropeanaBean(beanClass);
//        solrQuery.setFields(bean.getFieldStrings());
        // todo: set more like this
        return getSolrResponse(solrQuery);
    }

    private ResultPagination createPagination(QueryResponse response, SolrQuery query, String requestQueryString) {
        int numFound = (int) response.getResults().getNumFound();
        return new ResultPaginationImpl(query, numFound, requestQueryString);
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

    private String findSolrQueryType(String query) {
        // todo: finish this
        QueryType queryType = QueryType.SIMPLE_QUERY;
        return queryType.appearance;
    }

}