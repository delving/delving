package eu.europeana.web.controller;

import eu.europeana.query.Facet;
import eu.europeana.query.QueryExpression;
import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import eu.europeana.query.ResponseType;
import eu.europeana.query.ResultModel;
import eu.europeana.web.util.ControllerUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Class used for the Year grid Controller
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class YearGridController extends AbstractPortalController {
    private QueryModelFactory queryModelFactory;

    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("yeargrid");
        String queryString = request.getParameter("query");
        String query;
        if (queryString != null && !queryString.equalsIgnoreCase("")) {
            query = queryString;
        }
        else {
            query = "*:*";
        }
        String start = request.getParameter("start");
        if (start == null || start.equalsIgnoreCase("")) {
            start = "1";
        }
        String bobQuery = request.getParameter("bq");
         if (bobQuery != null && !bobQuery.equalsIgnoreCase("")) {
            model.put("bobQuery", bobQuery);
        }
        model.put("query", query);
        model.put("start", start);
        QueryModel queryModel = queryModelFactory.createQueryModel(QueryModelFactory.SearchType.SIMPLE);
        queryModel.setResponseType(ResponseType.FACETS_ONLY);
        queryModel.setStartRow(ControllerUtil.getStartRow(request));
        queryModel.setQueryExpression(new PrefabExpression(query));
        // make the request and interpret the results
        ResultModel resultModel = queryModel.fetchResult();
        List<Facet> facetList = resultModel.getFacets();
        model.put("facetList", facetList);
    }

    private static class PrefabExpression implements QueryExpression {
        private String query;

        private PrefabExpression(String query) {
            this.query = query;
        }

        public String getQueryString() {
            return query;
        }

        public String getBackendQueryString() {
            return query;
        }

        public QueryType getType() {
            return QueryType.ADVANCED_QUERY;
        }

        public boolean isMoreLikeThis() {
            return false;
        }
    }

}