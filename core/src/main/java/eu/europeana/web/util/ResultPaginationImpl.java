package eu.europeana.web.util;

import eu.europeana.query.PresentationQuery;
import eu.europeana.query.ResultPagination;
import org.apache.solr.client.solrj.SolrQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to compute result navigation to be inserted in the Freemarker Model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class ResultPaginationImpl implements ResultPagination {
    private static final String FACET_PROMPT = "&qf=";
    private static final int MARGIN = 5;
    private static final int PAGE_NUMBER_THRESHOLD = 7;
    private SolrQuery solrQuery;
    private boolean isPrevious;
    private int previousPage;
    private boolean isNext;
    private int nextPage;
    private int pageNumber;
    private int numFound;
    private int start;
    private List<Breadcrumb> breadcrumbs;
    private PresentationQueryImpl presentationQuery = new PresentationQueryImpl();
    private List<PageLink> pageLinks = new ArrayList<PageLink>();

    public ResultPaginationImpl(SolrQuery solrQuery, int numFound, String requestQueryString) {
        this.solrQuery = solrQuery;
        this.numFound = numFound;
        int rows = solrQuery.getRows();
        int totalPages = numFound / rows;
        if (numFound % rows != 0) {
            totalPages++;
        }
        start = 1;
        if (solrQuery.getStart() != null) {
            start = solrQuery.getStart() + 1; // solr is zero based so + 1
        }
        pageNumber = start / rows + 1;
        int fromPage = 1;
        int toPage = Math.min(totalPages, MARGIN * 2);
        if (pageNumber > PAGE_NUMBER_THRESHOLD) {
            fromPage = pageNumber - MARGIN;
            toPage = Math.min(pageNumber + MARGIN - 1, totalPages);
        }
        if (toPage - fromPage < MARGIN * 2 - 1) {
            fromPage = Math.max(1, toPage - MARGIN * 2 + 1);
        }
        this.isPrevious = start > 1;
        this.previousPage = start - rows;
        this.isNext = totalPages > 1 && pageNumber < toPage;
        this.nextPage = start + rows;
        for (int page = fromPage; page <= toPage; page++) {
            pageLinks.add(new PageLink(page, (page - 1) * rows + 1, pageNumber != page));
        }
        breadcrumbs = Breadcrumb.createList(solrQuery);
        presentationQuery.queryForPresentation = createQueryForPresentation(solrQuery);
        presentationQuery.queryToSave = requestQueryString;
        presentationQuery.userSubmittedQuery = solrQuery.getQuery();
        presentationQuery.typeQuery = removePresentationFilters(requestQueryString);
    }

    private String removePresentationFilters(String requestQueryString) {
        String[] filterQueries = requestQueryString.split("&");
        StringBuilder url = new StringBuilder();
        for (String filterQuery : filterQueries) {
            if (filterQuery.startsWith("qf=TYPE:")) {
                continue;
            }
            if (filterQuery.startsWith("tab=")) {
                continue;
            }
            if (filterQuery.startsWith("view=")) {
                continue;
            }
            if (filterQuery.startsWith("start=")) {
                continue; // start page must be reset to eliminate paging errors
            }
            url.append(filterQuery).append("&");
        }
        String urlString = url.toString().trim();
        if (urlString.endsWith("&")) {
            urlString = urlString.substring(0, urlString.length() - 1);
        }
        return urlString;
    }

    private String createQueryForPresentation(SolrQuery solrQuery) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("query").append("=").append(encode(solrQuery.getQuery()));
        // todo is this correct or should it be FacetQuery
        String[] facetQueries = ControllerUtil.getFilterQueriesWithoutPhrases(solrQuery);
        if (facetQueries != null) {
            for (String facetTerm : facetQueries) {
                queryString.append(FACET_PROMPT).append(facetTerm);
            }
        }
        return queryString.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPrevious() {
        return isPrevious;
    }

    @Override
    public int getPreviousPage() {
        return previousPage;
    }

    @Override
    public boolean isNext() {
        return isNext;
    }

    @Override
    public int getNextPage() {
        return nextPage;
    }

    @Override
    public int getLastViewableRecord() {
        return Math.min(nextPage - 1, numFound);
    }

    @Override
    public int getNumFound() {
        return numFound;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getRows() {
        return solrQuery.getRows();
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public List<PageLink> getPageLinks() {
        return pageLinks;
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs() {
        return breadcrumbs;
    }

    @Override
    public PresentationQuery getPresentationQuery() {
        return presentationQuery;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(isPrevious ? "previous=" + previousPage : "no-previous");
        out.append('\n');
        for (PageLink link : pageLinks) {
            out.append('\t');
            out.append(link.toString());
            out.append('\n');
        }
        out.append(isNext ? "next=" + previousPage : "no-next");
        out.append('\n');
        return out.toString();
    }

    private class PresentationQueryImpl implements PresentationQuery {
        private String userSubmittedQuery;
        private String queryForPresentation;
        private String queryToSave;
        private String typeQuery;

        @Override
        public String getUserSubmittedQuery() {
            return  userSubmittedQuery;
        }

        @Override
        public String getQueryForPresentation() {
            return queryForPresentation;
        }

        @Override
        public String getQueryToSave() {
            return queryToSave;
        }

        @Override
        public String getTypeQuery() {
            return typeQuery;
        }
    }

    public static class PageLink {
        private int start;
        private int display;
        private boolean linked;

        public PageLink(int display, int start, boolean linked) {
            this.display = display;
            this.start = start;
            this.linked = linked;
        }

        public int getDisplay() {
            return display;
        }

        public int getStart() {
            return start;
        }

        public boolean isLinked() {
            return linked;
        }

        public String toString() {
            if (linked) {
                return display + ":" + start;
            }
            else {
                return String.valueOf(display);
            }
        }
    }
}