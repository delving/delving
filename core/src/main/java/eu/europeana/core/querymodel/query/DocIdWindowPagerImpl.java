package eu.europeana.core.querymodel.query;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

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
    private List<Breadcrumb> breadcrumbs;
    private int fullDocUriInt;
//    @Value("#{europeanaProperties['portal.name']}")
    private String portalName = "portal"; // must be injected later

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public static DocIdWindowPager fetchPager(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery, SolrServer solrServer, Class<? extends DocId> idBean) throws SolrServerException, EuropeanaQueryException {
        DocIdWindowPagerImpl pager = new DocIdWindowPagerImpl();
        pager.query = originalBriefSolrQuery.getQuery();
        int fullDocUriInt = getFullDocInt(httpParameters, originalBriefSolrQuery, pager);
        int solrStartRow = getSolrStart(pager, fullDocUriInt);
        QueryResponse queryResponse = getQueryResponse(originalBriefSolrQuery, solrServer, pager, solrStartRow);
        if (queryResponse.getResults() == null) {
            return null; // if no results are found return null to signify that docIdPage can be created.
        }
        List<? extends DocId> list = queryResponse.getBeans(idBean);
        final SolrDocumentList response = queryResponse.getResults();
        int offset = (int) response.getStart();
        int numFound = (int) response.getNumFound();
        setNextAndPrevious(pager, fullDocUriInt, list, offset, numFound);
        pager.docIdWindow = new DocIdWindowImpl(list, offset, numFound);
        if (pager.hasNext) {
            pager.setNextFullDocUrl(httpParameters);
        }
        if (pager.hasPrevious) {
            pager.setPreviousFullDocUrl(httpParameters);
        }
        pager.fullDocUriInt = fullDocUriInt;
        return pager;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    static int getFullDocInt(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery, DocIdWindowPagerImpl pager) {
        pager.fullDocUri = fetchParameter(httpParameters, "uri", "");
        if (pager.fullDocUri.isEmpty()) {
            throw new IllegalArgumentException("Expected URI"); // todo: a better exception
        }
        pager.startPage = fetchParameter(httpParameters, "startPage", "1");
        pager.tab = fetchParameter(httpParameters, "tab", "all");
        pager.pageId = fetchParameter(httpParameters, "pageId", "");
        pager.format = fetchParameter(httpParameters, "format", "");
        pager.siwa = fetchParameter(httpParameters, "siwa", "");
        if (pager.pageId != null) {
            pager.setReturnToResults(httpParameters);
        }
        int fullDocUriInt = 0;
        if (!pager.startPage.isEmpty()) {
            fullDocUriInt = Integer.parseInt(fetchParameter(httpParameters, "start", ""));
            pager.setQueryStringForPaging(originalBriefSolrQuery, pager.startPage);
        }
        return fullDocUriInt;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private static int getSolrStart(DocIdWindowPagerImpl pager, int fullDocUriInt) {
        int solrStartRow = fullDocUriInt;
        pager.hasPrevious = fullDocUriInt > 1;
        if (fullDocUriInt > 1) {
            solrStartRow -= 2;
        }
        else {
            solrStartRow -= 1;
        }
        return solrStartRow;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private static void setNextAndPrevious(DocIdWindowPagerImpl pager, int fullDocUriInt, List<? extends DocId> list, int offset, int numFound) {
        if (offset + 2 < numFound) {
            pager.hasNext = true;
        }
        else if (fullDocUriInt == 1 && list.size() == 2) {
            pager.hasNext = true;
        }
        if (fullDocUriInt > numFound || list.size() < 2) {
            pager.hasPrevious = false;
            pager.hasNext = false;
        }
        if (pager.hasPrevious) {
            pager.previousInt = fullDocUriInt - 1;
            pager.previousUri = list.get(0).getEuropeanaUri();
        }
        if (pager.hasNext) {
            pager.nextInt = fullDocUriInt + 1;
            if (pager.hasPrevious) {
                pager.nextUri = list.get(2).getEuropeanaUri();
            }
            else {
                pager.nextUri = list.get(1).getEuropeanaUri();
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
     * @param pager
     * @param solrStartRow
     * @return
     * @throws EuropeanaQueryException
     * @throws SolrServerException
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private static QueryResponse getQueryResponse(SolrQuery originalBriefSolrQuery, SolrServer solrServer, DocIdWindowPagerImpl pager, int solrStartRow) throws EuropeanaQueryException, SolrServerException {
        originalBriefSolrQuery.setFields("europeana_uri");
        originalBriefSolrQuery.setStart(solrStartRow);
        originalBriefSolrQuery.setRows(3);
        pager.breadcrumbs = Breadcrumb.createList(originalBriefSolrQuery);
        return solrServer.query(originalBriefSolrQuery);
    }

    private void setReturnToResults(Map<String, String[]> httpParameters) {
        StringBuilder out = new StringBuilder();
        if (pageId.equalsIgnoreCase("brd")) {
            out.append(MessageFormat.format("/{0}/brief-doc.html?", portalName));
            out.append("query=").append(encode(query));
            final String[] filterQueries = httpParameters.get("qf");
            if (filterQueries != null) {
                for (String filterQuery : filterQueries) {
                    out.append("&qf=").append(filterQuery);
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
        out.append("&start=").append(startPage);
        String view = fetchParameter(httpParameters, "view", "");
        if (view.isEmpty()) {
            view = "table";
        }
        out.append("&view=").append(view);
        if (!tab.isEmpty()) {
            out.append("&tab=").append(tab);
        }
        if (!format.isEmpty()) {
            out.append("&format=").append(format);
        }
        if (!siwa.isEmpty()) {
            out.append("&siwa=").append(siwa);
        }
        out.append("&rtr=true");
        returnToResults = out.toString();
    }

    private void setNextFullDocUrl(Map<String, String[]> httpParameters) {
        StringBuilder out = new StringBuilder();
        out.append(MessageFormat.format("/{0}{1}.html?", portalName, nextUri.replaceAll("http://www.europeana.eu/resolve", "")));
        out.append("query=").append(encode(query));
        final String[] filterQueries = httpParameters.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                out.append("&qf=").append(filterQuery);
            }
        }
        out.append("&start=").append(nextInt);
        out.append("&startPage=").append(startPage);
        out.append("&pageId=").append(pageId);
        String view = fetchParameter(httpParameters, "view", "");
        if (view.isEmpty()) {
            view = "table";
        }
        out.append("&view=").append(view);
        if (!tab.isEmpty()) {
            out.append("&tab=").append(tab);
        }
        if (!format.isEmpty()) {
            out.append("&format=").append(format);
        }
        if (!siwa.isEmpty()) {
            out.append("&siwa=").append(siwa);
        }
        nextFullDocUrl = out.toString();
    }

    private void setPreviousFullDocUrl(Map<String, String[]> httpParameters) {
        StringBuilder out = new StringBuilder();
        out.append(MessageFormat.format("/{0}{1}.html?", portalName, previousUri.replaceAll("http://www.europeana.eu/resolve", "")));
        out.append("query=").append(encode(query));
        final String[] filterQueries = httpParameters.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                out.append("&qf=").append(filterQuery);
            }
        }
        out.append("&start=").append(previousInt);
        out.append("&startPage=").append(startPage);
        out.append("&pageId=").append(pageId);
        String view = fetchParameter(httpParameters, "view", "");
        if (view.isEmpty()) {
            view = "table";
        }
        out.append("&view=").append(view);
        if (tab.isEmpty()) {
            out.append("&tab=").append(tab);
        }
        previousFullDocUrl = out.toString();
    }

    private void setQueryStringForPaging(SolrQuery solrQuery, String startPage) {
        StringBuilder out = new StringBuilder();
        out.append("query=").append(encode(solrQuery.getQuery()));
        final String[] facetQueries = SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery);
        if (facetQueries != null) {
            for (String facetTerm : facetQueries) {
                out.append("&qf=").append(facetTerm);
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

    DocIdWindowPagerImpl() {
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