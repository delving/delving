package eu.europeana.beans.query;

import eu.europeana.beans.AnnotationProcessor;
import eu.europeana.beans.BriefBean;
import eu.europeana.beans.EuropeanaBean;
import eu.europeana.beans.FullBean;
import eu.europeana.beans.views.BriefBeanView;
import eu.europeana.beans.views.FullBeanView;
import eu.europeana.beans.views.GridBrowseBeanView;
import eu.europeana.database.dao.UserDaoImpl;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaId;
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
import java.util.regex.Pattern;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class BeanQueryModelFactory implements NewQueryModelFactory {
    private static final Pattern OR_PATTERN = Pattern.compile("\\s+[oO][rR]\\s+");
    private static final Pattern AND_PATTERN = Pattern.compile("\\s+[aA][nN][dD]\\s+");
    private static final Pattern NOT_START_PATTERN = Pattern.compile("^\\s*[nN][oO][tT]\\s+");
    private static final Pattern NOT_MIDDLE_PATTERN = Pattern.compile("\\s+[nN][oO][tT]\\s+");
    private CommonsHttpSolrServer solrServer;
    private AnnotationProcessor annotationProcessor;
    private UserDaoImpl dashboardDao;

    @Autowired
    public void setSolrServer(CommonsHttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Autowired
    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    @Autowired
    public void setDashboardDao(UserDaoImpl dashboardDao) {
        this.dashboardDao = dashboardDao;
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
        solrQuery.setQuery(sanitize(params.get("query")[0])); // only get the first one
        if (params.containsKey("start")) {
            solrQuery.setStart(Integer.valueOf(params.get("start")[0]));
        }
        if (params.containsKey("rows")) {
            solrQuery.setRows(Integer.valueOf(params.get("rows")[0]));
        }
        solrQuery.setQueryType(findSolrQueryType(solrQuery.getQuery()).toString());

        //set constraints
        final String[] filterQueries = params.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                solrQuery.addFacetQuery(filterQuery);
            }
        }
        return solrQuery;
    }

    @Override
    public SolrQuery createFromUri(String europeanaUri) throws EuropeanaQueryException {
        if (europeanaUri == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }
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
    public FullBeanView getFullResultView(SolrQuery solrQuery, Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException {
        if (params.get("uri") == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }
        String europeanaUri = params.get("uri")[0];
        solrQuery.setQuery("europeana_uri:\""+europeanaUri+"\"");
        solrQuery.setQueryType(QueryType.MORE_LIKE_THIS_QUERY.toString());
        return new FullBeanViewImpl(solrQuery, getSolrResponse(solrQuery, fullBean), params);  //TODO: implement this
    }

    // todo remove maybe use FullBeanView.getFullDoc instead
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
    public List<?> getDocIdList(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException {
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
        return queryResponse.getBeans(idBean);
    }


    public class BriefBeanViewImpl implements BriefBeanView {
        private ResultPagination pagination;
        private List<? extends BriefDoc> briefDocs;
        private List<FacetQueryLinks> queryLinks;

        @SuppressWarnings("unchecked")
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
        private QueryResponse solrResponse;
        private Map<String, String[]> params;
        private FullDoc fullDoc;
        private DocIdWindowPager docIdWindowPager;
        private List<? extends BriefDoc> relatedItems;

        private FullBeanViewImpl(SolrQuery solrQuery, QueryResponse solrResponse, Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException {
            this.solrResponse = solrResponse;
            this.params = params;
            fullDoc = createFullDoc();
            relatedItems = solrResponse.getBeans(BriefBean.class);
            docIdWindowPager = DocIdWindowPagerImpl.fetchPager(params, solrQuery, solrServer);
        }

        @Override
        public DocIdWindowPager getDocIdWindowPager() throws Exception {
            return docIdWindowPager;
        }

        @Override
        public List<? extends BriefDoc> getRelatedItems() {
            return relatedItems;
        }

        @Override
        public FullDoc getFullDoc() throws EuropeanaQueryException {
            return fullDoc;
        }

        private FullDoc createFullDoc() throws EuropeanaQueryException {
            SolrDocumentList matchDoc = (SolrDocumentList) solrResponse.getResponse().get("match");
            List<FullBean> fullBean = solrServer.getBinder().getBeans(FullBean.class, matchDoc);

            // if the record is not found give usefull error message
            if (fullBean.size() == 0) {
            EuropeanaId id = dashboardDao.fetchEuropeanaId(params.get("uri")[0]);
            if (id != null && id.isOrphan()) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_REVOKED.toString());
            } else if (id != null && id.getCollection().getCollectionState() != CollectionState.ENABLED) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_INDEXED.toString());
            } else {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
            }
        }
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
            solrQuery.setFacetMinCount(1);
            solrQuery.setFacetLimit(100);
            solrQuery.setRows(12); // todo replace with annotation later
            solrQuery.addFacetField(annotationProcessor.getFacetFieldStrings());
            EuropeanaBean bean = annotationProcessor.getEuropeanaBean(beanClass);
            solrQuery.setFields(bean.getFieldStrings());
            // todo: set more like this
            solrQuery.setQueryType(findSolrQueryType(solrQuery.getQuery()).toString());
        }
        if (beanClass == fullBean) {
        }
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

    private QueryType findSolrQueryType(String query) {
        // todo: finish this
        QueryType queryType = QueryType.SIMPLE_QUERY;
        return queryType;
    }

    String sanitize(String query) {
        StringBuilder out = new StringBuilder();
        for (int walk = 0; walk < query.length(); walk++) {
            char ch = query.charAt(walk);
            switch (ch) {
                case '{':
                case '}':
                    break;
                default:
                    out.append(ch);
            }
        }
        String q = out.toString();
        q = AND_PATTERN.matcher(q).replaceAll(" AND ");
        q = OR_PATTERN.matcher(q).replaceAll(" OR ");
        q = NOT_START_PATTERN.matcher(q).replaceAll("NOT ");
        q = NOT_MIDDLE_PATTERN.matcher(q).replaceAll(" NOT ");
        return q;
    }
}