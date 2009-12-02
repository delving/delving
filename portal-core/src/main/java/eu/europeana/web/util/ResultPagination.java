package eu.europeana.web.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to compute result navigation to be inserted in the Freemarker Model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class ResultPagination {
    private static final int MARGIN = 5;
    private static final int PAGE_NUMBER_THRESHOLD = 7;

    private boolean isPrevious;
    private int previousPage;
    private boolean isNext;
    private int nextPage;
    private int numFound;
    private int start;
    private List<PageLink> pageLinks = new ArrayList<PageLink>();
    private int rows;

    public ResultPagination(int numFound, int rows, int start) {
        this.numFound = numFound;
        this.start = start;
        this.rows = rows;
        int totalPages = numFound/rows;
        if (numFound % rows != 0) {
            totalPages++;
        }
        int pageNumber = start/rows + 1;
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
    }

    public boolean isPrevious() {
        return isPrevious;
    }

    public int getPreviousPage() {
        return previousPage;
    }

    public boolean isNext() {
        return isNext;
    }

    public int getNextPage() {
        return nextPage;
    }

    public int getLastViewableRecord() {
        return Math.min(nextPage-1,numFound);
    }

    public int getNumFound() {
        return numFound;
    }

    public int getRows() {
        return rows;
    }

    public int getStart() {
        return start;
    }

    public List<PageLink> getPageLinks() {
        return pageLinks;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(isPrevious ? "previous="+previousPage : "no-previous");
        out.append('\n');
        for (PageLink link : pageLinks) {
            out.append('\t');
            out.append(link.toString());
            out.append('\n');
        }
        out.append(isNext ? "next="+previousPage : "no-next");
        out.append('\n');
        return out.toString();
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
                return display+":"+start;
            }
            else {
                return String.valueOf(display);
            }
        }
    }
}