/*
 * Copyright 2011 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.core.querymodel.query;

import eu.delving.core.binding.SolrBindingService;
import eu.delving.metadata.RecordDefinition;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class DocIdWindowPagerImpl implements DocIdWindowPager {
    private DocIdWindow docIdWindow;
    private boolean hasNext;
    private String nextUri;
    private int nextInt;
    private boolean hasPrevious;
    private String previousUri;
    private int previousInt;
    private String fullDocUri;
    private String queryStringForPaging;
    private String startPage;
    private String query;
    private String returnToResults;
    private String nextFullDocUrl;
    private String previousFullDocUrl;
    private String pageId;
    private String format;
    private String siwa;
    private String tab;
    private String sortBy;
    private String theme;
    private List<Breadcrumb> breadcrumbs;
    private int fullDocUriInt;
    private int numFound;

    @Value("#{launchProperties['portal.name']}")
    private String portalName;

    @Override
    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    private SolrQuery copySolrQuery(SolrQuery solrQuery, RecordDefinition recordDefinition) {
        SolrQuery dCopy = solrQuery.getCopy();
        dCopy.setFilterQueries(SolrQueryUtil.getFilterQueriesAsOrQueries(solrQuery, recordDefinition.getFacetMap()));
        return dCopy;
    }

    @Override
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public void initialize(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery, SolrServer solrServer, RecordDefinition metadataModel) throws SolrServerException, EuropeanaQueryException {
        this.query = originalBriefSolrQuery.getQuery();
        int fullDocUriInt = getFullDocInt(httpParameters, originalBriefSolrQuery);
        this.fullDocUriInt = fullDocUriInt;
        int solrStartRow = getSolrStart(fullDocUriInt);
        QueryResponse queryResponse = getQueryResponse(copySolrQuery(originalBriefSolrQuery, metadataModel), solrServer, solrStartRow);
        if (queryResponse.getResults() == null) {
            throw new EuropeanaQueryException("no results for this query"); // if no results are found return null to signify that docIdPage can be created.
        }
        else if (queryResponse.getResults().size() == 0) {
            throw new EuropeanaQueryException("no results for this query"); // if no results are found return null to signify that docIdPage can be created.
        }
        List<? extends DocId> list = SolrBindingService.getDocIds(queryResponse);
        final SolrDocumentList response = queryResponse.getResults();
        this.breadcrumbs = Breadcrumb.createList(originalBriefSolrQuery); // todo comment out
        int offset = (int) response.getStart();
        int numFound = (int) response.getNumFound();
        this.numFound = numFound;
        setNextAndPrevious(fullDocUriInt, list, offset, numFound);
        this.docIdWindow = new DocIdWindowImpl(list, offset, numFound);
        if (this.hasNext) {
            this.setNextFullDocUrl(httpParameters);
        }
        if (this.hasPrevious) {
            this.setPreviousFullDocUrl(httpParameters);
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    int getFullDocInt(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery) {
        this.fullDocUri = fetchParameter(httpParameters, "uri", "");
        if (this.fullDocUri.isEmpty()) {
            throw new IllegalArgumentException("Expected URI"); // todo: a better exception
        }
        this.startPage = fetchParameter(httpParameters, "startPage", "1");
        this.tab = fetchParameter(httpParameters, "tab", "all");
        this.pageId = fetchParameter(httpParameters, "pageId", "");
        this.format = fetchParameter(httpParameters, "format", "");
        this.siwa = fetchParameter(httpParameters, "siwa", "");
        this.sortBy = fetchParameter(httpParameters, "sortBy", "");
        this.theme = fetchParameter(httpParameters, "theme", "");
        if (this.pageId != null) {
            this.setReturnToResults(httpParameters);
        }
        int fullDocUriInt = 0;
        if (!this.startPage.isEmpty()) {
            fullDocUriInt = Integer.parseInt(fetchParameter(httpParameters, "start", ""));
            this.setQueryStringForPaging(originalBriefSolrQuery, this.startPage);
        }
        return fullDocUriInt;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private int getSolrStart(int fullDocUriInt) {
        int solrStartRow = fullDocUriInt;
        this.hasPrevious = fullDocUriInt > 1;
        if (fullDocUriInt > 1) {
            solrStartRow -= 2;
        }
        else {
            solrStartRow -= 1;
        }
        return solrStartRow;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private void setNextAndPrevious(int fullDocUriInt, List<? extends DocId> list, int offset, int numFound) {
        if (offset + 2 < numFound) {
            this.hasNext = true;
        }
        else if (fullDocUriInt == 1 && list.size() == 2) {
            this.hasNext = true;
        }
        if (fullDocUriInt > numFound || list.size() < 2) {
            this.hasPrevious = false;
            this.hasNext = false;
        }
        if (this.hasPrevious) {
            this.previousInt = fullDocUriInt - 1;
            this.previousUri = list.get(0).getEuropeanaUri();
        }
        if (this.hasNext) {
            this.nextInt = fullDocUriInt + 1;
            if (this.hasPrevious) {
                this.nextUri = list.get(2).getEuropeanaUri();
            }
            else {
                this.nextUri = list.get(1).getEuropeanaUri();
            }
        }
    }

    /**
     * This method queries the SolrSearch engine to get a QueryResponse with 3 DocIds
     * <p/>
     * This method does not have to be Unit Tested
     *
     * @param originalBriefSolrQuery
     * @param solrServer
     * @param solrStartRow
     * @return
     * @throws EuropeanaQueryException
     * @throws org.apache.solr.client.solrj.SolrServerException
     *
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private QueryResponse getQueryResponse(SolrQuery originalBriefSolrQuery, SolrServer solrServer, int solrStartRow) throws EuropeanaQueryException, SolrServerException {
        originalBriefSolrQuery.setFields("europeana_uri");
        originalBriefSolrQuery.setStart(solrStartRow);
        originalBriefSolrQuery.setRows(3);
//        this.breadcrumbs = Breadcrumb.createList(originalBriefSolrQuery); //todo decide for or queries
        return solrServer.query(originalBriefSolrQuery);
    }

    private void setReturnToResults(Map<String, String[]> httpParameters) {
        StringBuilder out = new StringBuilder();
        if (pageId.equalsIgnoreCase("brd")) {
            out.append(MessageFormat.format("/{0}/search?", portalName));
            out.append("query=").append(encode(query));
            final String[] filterQueries = httpParameters.get("qf");
            if (filterQueries != null) {
                for (String filterQuery : filterQueries) {
                    out.append("&amp;qf=").append(filterQuery);
                }
            }
        }
        else if (pageId.equalsIgnoreCase("yg")) {
            out.append(MessageFormat.format("/{0}/year-grid.html?", portalName));
            if (query.length() > 4) {
                String userQueryString = query.replaceFirst("^\\d{4}", "").trim();
                out.append("query=").append(encode(userQueryString)).append("&");
            }
            out.append("bq=").append(encode(query));
        }
        out.append("&amp;start=").append(startPage);
        String view = fetchParameter(httpParameters, "view", "");
        if (view.isEmpty()) {
            view = "table";
        }
        out.append("&amp;view=").append(view);
        if (!tab.isEmpty()) {
            out.append("&amp;tab=").append(tab);
        }
        if (!format.isEmpty()) {
            out.append("&amp;format=").append(format);
        }
        if (!siwa.isEmpty()) {
            out.append("&amp;siwa=").append(siwa);
        }
        if (!sortBy.isEmpty()) {
            out.append("&amp;sortBy=").append(sortBy);
        }
        if (!theme.isEmpty()) {
            out.append("&amp;theme=").append(theme);
        }
        out.append("&amp;rtr=true");
        returnToResults = out.toString();
    }

    private void setNextFullDocUrl(Map<String, String[]> httpParameters) {
        StringBuilder out = new StringBuilder();
        out.append(MessageFormat.format("/{0}/object/{1}.html?", portalName, nextUri));
        out.append("query=").append(encode(query));
        final String[] filterQueries = httpParameters.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                out.append("&amp;qf=").append(filterQuery);
            }
        }
        out.append("&amp;start=").append(nextInt);
        out.append("&amp;startPage=").append(startPage);
        out.append("&amp;pageId=").append(pageId);
        out.append("&amp;sortBy=").append(sortBy);
        String view = fetchParameter(httpParameters, "view", "");
        if (view.isEmpty()) {
            view = "table";
        }
        out.append("&amp;view=").append(view);
        if (!tab.isEmpty()) {
            out.append("&amp;tab=").append(tab);
        }
        if (!format.isEmpty()) {
            out.append("&amp;format=").append(format);
        }
        if (!siwa.isEmpty()) {
            out.append("&amp;siwa=").append(siwa);
        }
        nextFullDocUrl = out.toString();
    }

    private void setPreviousFullDocUrl(Map<String, String[]> httpParameters) {
        StringBuilder out = new StringBuilder();
        out.append(MessageFormat.format("/{0}/object/{1}.html?", portalName, previousUri));
        out.append("query=").append(encode(query));
        final String[] filterQueries = httpParameters.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                out.append("&amp;qf=").append(filterQuery);
            }
        }
        out.append("&amp;start=").append(previousInt);
        out.append("&amp;startPage=").append(startPage);
        out.append("&amp;pageId=").append(pageId);
        out.append("&amp;sortBy=").append(sortBy);
        String view = fetchParameter(httpParameters, "view", "");
        if (view.isEmpty()) {
            view = "table";
        }
        out.append("&amp;view=").append(view);
        if (tab.isEmpty()) {
            out.append("&amp;tab=").append(tab);
        }
        previousFullDocUrl = out.toString();
    }

    private void setQueryStringForPaging(SolrQuery solrQuery, String startPage) {
        StringBuilder out = new StringBuilder();
        out.append("query=").append(encode(solrQuery.getQuery()));
        final String[] facetQueries = SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery);
        if (facetQueries != null) {
            for (String facetTerm : facetQueries) {
                out.append("&amp;qf=").append(facetTerm);
            }
        }
        out.append("&startPage=").append(startPage);
        this.queryStringForPaging = out.toString();
    }

    private static String fetchParameter(Map<String, String[]> httpParameters, String key, String defaultValue) {
        String[] array = httpParameters.get(key);
        if (array == null || array.length == 0) {
            return defaultValue;
        }
        else {
            return array[0];
        }
    }

    private static String encode(String string) {
        try {
            return URLEncoder.encode(string, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public DocIdWindowPagerImpl() {
    }

    @Override
    public DocIdWindow getDocIdWindow() {
        return docIdWindow;
    }

    @Override
    public String getStartPage() {
        return startPage;
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs() {
        return breadcrumbs;
    }

    @Override
    public int getNumFound() {
        return numFound;
    }

    @Override
    public boolean isNext() {
        return hasNext;
    }

    @Override
    public boolean isPrevious() {
        return hasPrevious;
    }

    @Override
    public String getQueryStringForPaging() {
        return queryStringForPaging;
    }

    @Override
    public String getFullDocUri() {
        return fullDocUri;
    }

    @Override
    public String getNextFullDocUrl() {
        return nextFullDocUrl;
    }

    @Override
    public String getPreviousFullDocUrl() {
        return previousFullDocUrl;
    }

    @Override
    public String getNextUri() {
        return nextUri;
    }

    @Override
    public int getNextInt() {
        return nextInt;
    }

    @Override
    public String getPreviousUri() {
        return previousUri;
    }

    @Override
    public int getPreviousInt() {
        return previousInt;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public String getReturnToResults() {
        return returnToResults;
    }

    @Override
    public String getPageId() {
        return pageId;
    }

    @Override
    public String getTab() {
        return tab;
    }

    @Override
    public String getSortBy() {
        return sortBy;
    }

    // todo fix this it throws an nullPointerException now

    @Override
    public String toString() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("query", query);
        map.put("queryStringForPaging", queryStringForPaging);
        map.put("fullDocUri", fullDocUri);
        map.put("fullDocStart", String.valueOf(docIdWindow.getOffset()));
        map.put("hitCount", String.valueOf(docIdWindow.getHitCount()));
        map.put("isPrevious", String.valueOf(hasPrevious));
        map.put("previousInt", String.valueOf(previousInt));
        map.put("previousUri", String.valueOf(previousUri));
        map.put("isNext", String.valueOf(hasNext));
        map.put("nextInt", String.valueOf(nextInt));
        map.put("nextUri", String.valueOf(nextUri));
        map.put("returnToResults", returnToResults);
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n");
        }
        return out.toString();
    }

    @Override
    public int getFullDocUriInt() {
        return fullDocUriInt;
    }

    private static class DocIdWindowImpl implements DocIdWindow {

        private List<? extends DocId> docIds;
        private int offset;
        private int hitCount;

        private DocIdWindowImpl(List<? extends DocId> docIds, int offset, int hitCount) {
            this.docIds = docIds;
            this.offset = offset;
            this.hitCount = hitCount;
        }

        @Override
        public List<? extends DocId> getIds() {
            return docIds;
        }

        @Override
        public Integer getOffset() {
            return offset;
        }

        @Override
        public Integer getHitCount() {
            return hitCount;
        }
    }
}