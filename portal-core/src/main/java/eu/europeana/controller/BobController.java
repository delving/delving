package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.controller.util.ResultPagination;
import eu.europeana.query.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Class used for the Brilliant Object Browser (a.k.a BoB) Controller
 *
 * @author Sjoerd Siebinga
 */

public class BobController extends AbstractPortalController {
    private static final String [] DEFAULT_YEAR_QUERIES = new String[] {
            "YEAR:1915","YEAR:1980","YEAR:2000","YEAR:1977","YEAR:1945",
    };
    private static final String [] DEFAULT_TAG_QUERIES = new String[] {
            "USERTAGS:\"moyen age\"","USERTAGS:\"moulage\"",
    };
    private QueryModelFactory queryModelFactory;

    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("bob-new");
        String queryString = request.getParameter("query");
        String query = null;
        String queryType = request.getParameter("qt");
        if (queryString != null) {
            query = queryString;
        }
        else if (queryType != null) {
            if (queryType.equalsIgnoreCase("year")) {
                query = DEFAULT_YEAR_QUERIES[(int) (Math.random() * DEFAULT_YEAR_QUERIES.length)];
            }
            else if (queryType.equalsIgnoreCase("tags")) {
                query = DEFAULT_TAG_QUERIES[(int) (Math.random() * DEFAULT_TAG_QUERIES.length)];
            }
        }
        else {
            query = DEFAULT_YEAR_QUERIES[(int) (Math.random() * DEFAULT_YEAR_QUERIES.length)];
        }
        QueryModel queryModel = queryModelFactory.createQueryModel(QueryModelFactory.SearchType.SIMPLE);
        queryModel.setResponseType(ResponseType.LARGE_BRIEF_DOC_WINDOW);
        queryModel.setStartRow(ControllerUtil.getStartRow(request));
        queryModel.setQueryExpression(new PrefabExpression(query));
        // make the request and interpret the results
        ResultModel resultModel = queryModel.fetchResult();

        BriefDocWindow briefDocWindow = resultModel.getBriefDocWindow();
        List<BriefDoc> docList = briefDocWindow.getDocs();

        ResultPagination pagination = new ResultPagination(
                resultModel.getBriefDocWindow().getHitCount(),
                queryModel.getRows(),
                queryModel.getStartRow() + 1
        );
        model.put("query", query);
        model.put("startPage", queryModel.getStartRow() + 1);
        model.put("docList", docList);
        model.put("pagination", pagination);
    }

    private static class PrefabExpression implements QueryExpression {
        private String query;

        private PrefabExpression(String query) {
            this.query = query + " -europeana_hasObject:false";
        }

        public String getQueryString() {
            return query;
        }

        public String getBackendQueryString() {
            return query;
        }

        public Type getType() {
            return Type.ADVANCED_QUERY;
        }

        public boolean isMoreLikeThis() {
            return false;
        }
    }

}