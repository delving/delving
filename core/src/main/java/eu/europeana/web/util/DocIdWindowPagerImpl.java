package eu.europeana.web.util;

import eu.europeana.beans.IdBean;
import eu.europeana.query.DocIdWindow;
import eu.europeana.query.DocIdWindowPager;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private String pageId;
    private String tab;

    public static DocIdWindowPager fetchPager(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery, SolrServer solrServer) throws SolrServerException {
        DocIdWindowPagerImpl pager = new DocIdWindowPagerImpl();
        pager.query = originalBriefSolrQuery.getQuery();
        pager.fullDocUri = fetchParameter(httpParameters, "uri", "");
        if (pager.fullDocUri.isEmpty()) {
            throw new IllegalArgumentException("Expected URI"); // todo: a better exception
        }
        pager.startPage = fetchParameter(httpParameters, "startPage", "1");
        pager.tab = fetchParameter(httpParameters, "tab", "all");
        pager.pageId = fetchParameter(httpParameters, "pageId", "");
        if (pager.pageId != null) {
            pager.setReturnToResults(httpParameters);
        }
        String start = fetchParameter(httpParameters, "start", "");
        int fullDocUriInt = 0;
        if (!pager.startPage.isEmpty()) {
            fullDocUriInt = Integer.parseInt(start);
            pager.setQueryStringForPaging(originalBriefSolrQuery);
        }
        int solrStartRow = fullDocUriInt;
        pager.hasPrevious = fullDocUriInt > 1;
        if (fullDocUriInt > 0) {
            solrStartRow -= 1;
        }
        originalBriefSolrQuery.setFields("europeana_uri");
        originalBriefSolrQuery.setStart(solrStartRow);
        originalBriefSolrQuery.setRows(3);
        QueryResponse queryResponse = solrServer.query(originalBriefSolrQuery);
        List<IdBean> list = queryResponse.getBeans(IdBean.class);
        final SolrDocumentList response = queryResponse.getResults();
        int offset = (int) response.getStart();
        int numFound = (int) response.getNumFound();
        pager.hasNext = offset + 1 < numFound;
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
        pager.docIdWindow = new DocIdWindowImpl(list, offset, numFound);
        return pager;
    }

    private void setReturnToResults(Map<String, String[]> httpParameters)  {
        StringBuilder out = new StringBuilder();
        if (pageId.equalsIgnoreCase("brd")) {
            out.append("brief-doc.html?");
            out.append("query=").append(encode(query));
            final String[] filterQueries = httpParameters.get("qf");
            if (filterQueries != null) {
                for (String filterQuery : filterQueries) {
                    out.append("&qf=").append(filterQuery);
                }
            }
        }
        else if (pageId.equalsIgnoreCase("yg")) {
            out.append("year-grid.html?");
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
        if (tab.isEmpty()) {
            out.append("&tab=").append(tab);
        }
        returnToResults = out.toString();
    }

    private void setQueryStringForPaging(SolrQuery solrQuery) {
        StringBuilder out = new StringBuilder();
        out.append("query=").append(encode(solrQuery.getQuery()));
        final String[] facetQueries = ControllerUtil.getFilterQueriesWithoutPhrases(solrQuery);
        if (facetQueries != null) {
            for (String facetTerm : facetQueries) {
                out.append("&qf=").append(facetTerm);
            }
        }
        out.append("&startPage=").append(startPage);
        queryStringForPaging = out.toString();
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

    private DocIdWindowPagerImpl() {
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

    private static class DocIdWindowImpl implements DocIdWindow {

        private List<IdBean> ids;
        private int offset;
        private int hitCount;

        private DocIdWindowImpl(List<IdBean> ids, int offset, int hitCount) {
            this.ids = ids;
            this.offset = offset;
            this.hitCount = hitCount;
        }

        @Override
        public List<IdBean> getIds() {
            return ids;
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