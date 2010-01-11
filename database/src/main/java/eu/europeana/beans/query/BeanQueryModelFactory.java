package eu.europeana.beans.query;

import eu.europeana.beans.AnnotationProcessor;
import eu.europeana.beans.BriefBean;
import eu.europeana.beans.EuropeanaField;
import eu.europeana.beans.FullBean;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class BeanQueryModelFactory implements NewQueryModelFactory {

    // new solrJ implementation methods
    @Autowired
    private CommonsHttpSolrServer solrServer;

    public void setSolrServer(CommonsHttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }


    @Autowired
    private AnnotationProcessor annotationProcessor;

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

    private Class<?> briefBean;

    public void setBriefBean(Class<?> briefBean) {
        this.briefBean = briefBean;
    }

    private Class<?> fullBean;

    public void setFullBean(Class<?> fullBean) {
        this.fullBean = fullBean;
    }

    private Class<?> IdBean;

    public void setIdBean(Class<?> idBean) {
        IdBean = idBean;
    }

    @Override
    public BriefBeanView getBriefResultView(SolrQuery solrQuery) throws EuropeanaQueryException {
        return new BriefBeanViewImpl(solrQuery, getSolrResponse(solrQuery, briefBean));
    }

    @Override
    public FullBeanView getFullResultView(SolrQuery solrQuery, Map<String, String[]> params) throws EuropeanaQueryException {
        return new FullBeanViewImpl(solrQuery, getSolrResponse(solrQuery, fullBean), params);  //TODO: implement this
    }

    @Override
    public GridBrowseBeanView getGridBrowseResultView(SolrQuery solrQuery) throws EuropeanaQueryException {
        return null;  //TODO: implement this
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
            return (List<? extends BriefDoc>) solrResponse.getBeans(briefBean);
        }

        @Override
        public List<FacetQueryLinks> getQueryFacetsLinks() throws UnsupportedEncodingException {
            return FacetQueryLinks.createDecoratedFacets(solrQuery, solrResponse.getFacetFields());
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
            return new DocIdWindowPagerImpl(params, createFromQueryParams(params), solrServer);
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
            throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), e);
        }
        return queryResponse;
    }

    @Override
    public QueryResponse getSolrResponse(SolrQuery solrQuery, Class<?> beanClass) throws EuropeanaQueryException { // add bean to ???
        // set facets
        if (beanClass == briefBean) {
            Set<? extends EuropeanaField> facetFields = annotationProcessor.getFacetFields();
            ArrayList<String> facets = new ArrayList<String>();
            for (EuropeanaField facetField : facetFields) {
                facets.add(facetField.getFieldNameString());
            }
            String[] facetArr = facets.toArray(new String[facets.size()]);
            solrQuery.setFacet(true);
            solrQuery.addFacetField(facetArr);
        }

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


        return getSolrResponse(solrQuery);
    }

    private ResultPagination createPagination(QueryResponse response, SolrQuery query) {
        int numFound = (int) response.getResults().getNumFound();
        return new ResultPaginationImpl(numFound, query.getStart(), query.getRows());
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