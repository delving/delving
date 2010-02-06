/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.beans.query;

import eu.europeana.beans.BriefBean;
import eu.europeana.beans.FullBean;
import eu.europeana.beans.IdBean;
import eu.europeana.beans.annotation.AnnotationProcessor;
import eu.europeana.beans.annotation.EuropeanaBean;
import eu.europeana.database.dao.UserDaoImpl;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.query.*;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.DocIdWindowPagerImpl;
import eu.europeana.web.util.FacetQueryLinks;
import eu.europeana.web.util.ResultPaginationImpl;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@SuppressWarnings({"ValueOfIncrementOrDecrementUsed"})
public class BeanQueryModelFactory implements QueryModelFactory {
    private Logger log = Logger.getLogger(getClass());
    private QueryAnalyzer queryAnalyzer;
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

    @Autowired
    public void setQueryAnalyzer(QueryAnalyzer queryAnalyzer) {
        this.queryAnalyzer = queryAnalyzer;
    }

    /**
     * create solr query from http query parameters
     */
    @Override
    public SolrQuery createFromQueryParams(Map<String, String[]> params) throws EuropeanaQueryException {
        SolrQuery solrQuery = new SolrQuery();
        if (params.containsKey("query") || params.containsKey("query1")) {
            if (!params.containsKey("query1")) {
                solrQuery.setQuery(queryAnalyzer.sanitize(params.get("query")[0])); // only get the first one
            }
            else { // support advanced search
                solrQuery.setQuery(queryAnalyzer.createAdvancedQuery(params));
            }
        }
        else {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString());
        }

        if (params.containsKey("start")) {
            solrQuery.setStart(Integer.valueOf(params.get("start")[0]));
        }
        else {
            solrQuery.setStart(0);
        }

        if (params.containsKey("rows")) {
            solrQuery.setRows(Integer.valueOf(params.get("rows")[0]));
        }
        solrQuery.setQueryType(queryAnalyzer.findSolrQueryType(solrQuery.getQuery()).toString());

        //set constraints
        final String[] filterQueries = params.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                solrQuery.addFilterQuery(filterQuery);
            }
        }
        solrQuery.setFilterQueries(ControllerUtil.getFilterQueriesAsPhrases(solrQuery)); // todo: integrate into the above loop
        return solrQuery;
    }

    @Override
    public SolrQuery createFromUri(String europeanaUri) throws EuropeanaQueryException {
        if (europeanaUri == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("europeana_uri:\"" + europeanaUri + "\"");
        solrQuery.setQueryType(QueryType.MORE_LIKE_THIS_QUERY.toString());
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
    public FullBeanView getFullResultView(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException {
        if (params.get("uri") == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }
        String europeanaUri = params.get("uri")[0];
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("europeana_uri:\"" + europeanaUri + "\"");
        solrQuery.setQueryType(QueryType.MORE_LIKE_THIS_QUERY.toString());
        return new FullBeanViewImpl(solrQuery, getSolrResponse(solrQuery, fullBean), params);
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

    @Override
    public List<?> getDocIdList(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException {
        SolrQuery solrQuery = createFromQueryParams(params);
        Integer start = solrQuery.getStart();
        if (start > 1) {
            solrQuery.setStart(start - 2);
        }
        solrQuery.setRows(3);
        solrQuery.setFields("europeana_uri");
        // Fetch results from server
        QueryResponse queryResponse = solrServer.query(solrQuery);
        // fetch beans
        return queryResponse.getBeans(idBean);
    }

    /**
     * Get records from Sorl for a particular collection for the siteMap.
     *
     * @param europeanaCollectionName the europeana collectionName as stored in the EuropeanaCollection Domain object
     * @param rowsToBeReturned number of rows to be returned from Solr
     * @param pageNumber which page of the sitemap per collection will be returned.
     * @return list of IdBeans
     * @throws EuropeanaQueryException
     * @throws SolrServerException
     */
    @Override
    public SiteMapBeanView getSiteMapBeanView(String europeanaCollectionName, int rowsToBeReturned, int pageNumber) throws EuropeanaQueryException, SolrServerException  {
        SolrQuery solrQuery = new SolrQuery("europeana_collectionName:"+europeanaCollectionName);
        solrQuery.setRows(rowsToBeReturned);
        solrQuery.setFields("europeana_uri", "timestamp");
        solrQuery.setStart(pageNumber * rowsToBeReturned);
        QueryResponse queryResponse = solrServer.query(solrQuery);
        return new SiteMapBeanViewImpl(europeanaCollectionName, queryResponse, rowsToBeReturned);
    }

    public class SiteMapBeanViewImpl implements SiteMapBeanView {
        private String europeanaCollectionName;
        private List<IdBean> idBeans;
        private int numFound;
        private int maxPageForCollection;

        public SiteMapBeanViewImpl(String europeanaCollectionName, QueryResponse response, int rowsToBeReturned) {
            this.europeanaCollectionName = europeanaCollectionName;
            this.numFound = (int) response.getResults().getNumFound();
            this.idBeans = response.getBeans(IdBean.class);
            this.maxPageForCollection = numFound / rowsToBeReturned + 1;
        }

        @Override
        public List<IdBean> getIdBeans() {
            return idBeans;
        }

        @Override
        public int getNumFound() {
            return numFound;
        }

        @Override
        public String getCollectionName() {
            return europeanaCollectionName;
        }

        @Override
        public int getMaxPageForCollection() {
            return maxPageForCollection;
        }
    }


    public class BriefBeanViewImpl implements BriefBeanView {
        private ResultPagination pagination;
        private List<? extends BriefDoc> briefDocs;
        private List<FacetQueryLinks> queryLinks;
        private Map<String, String> facetLogs;

        @SuppressWarnings("unchecked")
        private BriefBeanViewImpl(SolrQuery solrQuery, QueryResponse solrResponse, String requestQueryString) throws UnsupportedEncodingException {
            pagination = createPagination(solrResponse, solrQuery, requestQueryString);
            briefDocs = addIndexToBriefDocList(solrQuery, (List<? extends BriefDoc>) solrResponse.getBeans(briefBean));
            queryLinks = FacetQueryLinks.createDecoratedFacets(solrQuery, solrResponse.getFacetFields());
            facetLogs = createFacetLogs(solrResponse);
        }

        private Map<String, String> createFacetLogs(QueryResponse solrResponse) {
            Map<String, String> facetLogs = new HashMap<String, String>();
            List<FacetField> facetFieldList = solrResponse.getFacetFields();
            for (FacetField facetField : facetFieldList) {
                if (facetField.getName().equalsIgnoreCase("LANGUAGE") || facetField.getName().equalsIgnoreCase("COUNTRY")) {
                    StringBuilder out = new StringBuilder();
                    List<FacetField.Count> list = facetField.getValues();
                    if (list == null) {
                        break;
                    }
                    int counter = 0;
                    for (FacetField.Count count : list) {
                        counter++;
                        out.append(count.toString()).append(",");
                        if (counter > 5) {
                            break;
                        }
                    }
                    facetLogs.put(facetField.getName(), out.toString().substring(0, out.toString().length() -1));
                }
            }
            return facetLogs;
        }

        @Override
        public List<? extends BriefDoc> getBriefDocs() {
            return briefDocs;
        }

        @Override
        public List<FacetQueryLinks> getFacetQueryLinks() {
            return queryLinks;
        }

        @Override
        public ResultPagination getPagination() {
            return pagination;
        }

        @Override
        public Map<String, String> getFacetLogs() {
            return facetLogs;
        }
    }

    List<? extends BriefDoc> addIndexToBriefDocList(SolrQuery solrQuery, List<? extends BriefDoc> briefDocList) {
        Integer start = solrQuery.getStart();
        int index = start == null ? 1 : start + 1;
        for (BriefDoc briefDoc : briefDocList) {
            briefDoc.setIndex(index++);
        }
        return briefDocList;
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
            relatedItems = addIndexToBriefDocList(solrQuery, solrResponse.getBeans(BriefBean.class));
            docIdWindowPager = createDocIdPager(params);
        }

        private DocIdWindowPager createDocIdPager(Map<String, String[]> params) throws SolrServerException, EuropeanaQueryException {
            DocIdWindowPager idWindowPager = null;
            if (params.containsKey("query")) {
                idWindowPager = DocIdWindowPagerImpl.fetchPager(params, createFromQueryParams(params), solrServer);
            }
            return idWindowPager;
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
                }
                else if (id != null && id.getCollection().getCollectionState() != CollectionState.ENABLED) {
                    throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_INDEXED.toString());
                }
                else {
                    throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
                }
            }
            return fullBean.get(0);
        }
    }

    @Override
    public QueryResponse getSolrResponse(SolrQuery solrQuery) throws EuropeanaQueryException {
//        if (solrQuery.getStart() < 1) {
//            solrQuery.setStart(0);
//            log.warn("Solr Start cannot be negative");
//        }
        // solr query is 0 based
        if (solrQuery.getStart() != null && solrQuery.getStart() > 0) {
            solrQuery.setStart(solrQuery.getStart() - 1);
        }
        QueryResponse queryResponse;
        try {
            queryResponse = solrServer.query(solrQuery);
        }
        catch (SolrException e) {
            log.error("unable to execute SolrQuery", e);
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString(), e);
        }
        catch (SolrServerException e) {
            //todo determine which errors the SolrServer can throw
            log.error("Unable to fetch result", e);
            if (e.getMessage().equalsIgnoreCase("Error executing query")) {
                throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString(), e);
            }
            else {
                throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), e);
            }
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
            if (solrQuery.getQueryType().equalsIgnoreCase(QueryType.SIMPLE_QUERY.toString())) {
                solrQuery.setQueryType(queryAnalyzer.findSolrQueryType(solrQuery.getQuery()).toString());
            }
        }
        if (beanClass == fullBean) {
        }
        return getSolrResponse(solrQuery);
    }

    private ResultPagination createPagination(QueryResponse response, SolrQuery query, String requestQueryString) {
        int numFound = (int) response.getResults().getNumFound();
        return new ResultPaginationImpl(query, numFound, requestQueryString);
    }
}