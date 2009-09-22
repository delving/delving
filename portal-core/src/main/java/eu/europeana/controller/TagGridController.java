package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.query.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Class used for the Tag grid Controller
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class TagGridController extends AbstractPortalController {
    private QueryModelFactory queryModelFactory;

    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("tag-grid");
        String queryString = request.getParameter("query");
        String query;
        if (queryString != null) {
            query = queryString;
        }
        else {
            query = "*:*";
        }
        model.put("query", query);
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

        public Type getType() {
            return Type.ADVANCED_QUERY;
        }

        public boolean isMoreLikeThis() {
            return false;
        }
    }

}