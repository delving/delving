package eu.europeana.web.controller;

import eu.europeana.json.JsonResultModel;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import eu.europeana.query.QueryProblem;
import eu.europeana.query.ResponseType;
import eu.europeana.query.ResultModel;
import eu.europeana.web.util.CQL2Lucene;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.FormatType;
import eu.europeana.web.util.NextQueryFacet;
import eu.europeana.web.util.QueryConstraints;
import eu.europeana.web.util.ResultPagination;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * This controller manages searching and browsing of the documents, making HTTP requests to
 * the data store.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class BriefDocController extends AbstractPortalController {
    private static final String QUERY_PARAMETER = "query";
    private static final String FACET_PARAMETER = "facet";
    private static final String OPERATOR_PARAMETER = "operator";
    private static final String FORMAT_PARAMETER = "format";
    private static final String DISPLAY_PARAMETER = "display";
    private static final String RESULT_MODEL_PARAM = "result";
    private static final String SRU_OPERATION_PARMETER = "operation";
    private static final String NEXT_QUERY_FACETS_PARAM = "nextQueryFacets";
    private static final String QUERY_TO_SAVE = "queryToSave";
    private static final String QUERY_STRING_FOR_PRESENTATION_PARAM = "queryStringForPresentation";
    private static final String BREADCRUMBS_PARAM = "breadcrumbs";
    private static final String PAGINATION_PARAM = "pagination";
    private static final String SERVLET_URL_PARAM = "servletUrl";

    private Logger log = Logger.getLogger(getClass());
    private QueryModelFactory queryModelFactory;

    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        // make sure there's a query, and sanitize it
        String query = getQuery(request);

        // determine if the query string is in CQL query syntax and convert it to a Lucene query
        if (ControllerUtil.getParameter(request, SRU_OPERATION_PARMETER) != null) {
            log.info("translating CQL to Lucene");
            query = CQL2Lucene.translate(query);
        }
        if (query == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString());
            // todo later for redering the query syntax help page
//            log.info("no query, we'll do help");
//            model.setView("brief-doc-window-help");
//            model.put("formatTypes", FormatType.values());
//            return;
        }
        QueryConstraints queryConstraints = new QueryConstraints(request.getParameterValues(QueryConstraints.PARAM_KEY));
        // check if we are dealing with a request from the advanced search page
        QueryModelFactory.SearchType type = QueryModelFactory.SearchType.SIMPLE;
        if (request.getParameter(QUERY_PARAMETER) == null) {
            type = QueryModelFactory.SearchType.ADVANCED;
        }

        // construct query model
        QueryModel queryModel = queryModelFactory.createQueryModel(type);
        queryModel.setResponseType(ResponseType.SMALL_BRIEF_DOC_WINDOW);
        queryModel.setStartRow(ControllerUtil.getStartRow(request));
        int rows = ControllerUtil.getRows(request);
        if (rows > 0) {
            queryModel.setRows(rows);
        }
        queryModel.setQueryConstraints(queryConstraints);

        ResultModel resultModel;
        try {
            queryModel.setQueryString(query);
            // execute query
            resultModel = queryModel.fetchResult();
        }
        catch (EuropeanaQueryException exception) {
            // invalid query string
            // construct empty result model with error
            QueryProblem queryProblem = QueryProblem.NONE;
            if (exception != null) {
                queryProblem = exception.getFetchProblem();
            }
            if (queryProblem == QueryProblem.SOLR_UNREACHABLE) {
                throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), exception);
            }
            else {
                resultModel = new JsonResultModel(null, ResponseType.SMALL_BRIEF_DOC_WINDOW, true, "Invalid query string.");
            }
        }


        List<NextQueryFacet> nextQueryFacets;
        ResultPagination resultPagination = new ResultPagination(
                resultModel.getBriefDocWindow().getHitCount(),
                queryModel.getRows(),
                queryModel.getStartRow() + 1
        );
        nextQueryFacets = NextQueryFacet.createDecoratedFacets(
                resultModel.getFacets(),
                queryConstraints
        );
        // todo: when moving to solr 1.4 beta remove this loop and move the first part out. We use mulitvalue select instead.
//        if (queryConstraints.getEntries().isEmpty()) {
//            // if there were no contraints we have to get the next query facets from the regular query
//            nextQueryFacets = NextQueryFacet.createDecoratedFacets(
//                    resultModel.getFacets(),
//                    queryConstraints
//            );
//        }
//        else {
//            // only pre-fetch facets when there are query constraints
//            queryModel.setRows(0);
//            queryModel.setResponseType(ResponseType.FACETS_ONLY);
//            queryModel.setQueryConstraints(null);
//            nextQueryFacets = NextQueryFacet.createDecoratedFacets(
//                    queryModel.fetchResult().getFacets(),
//                    queryConstraints
//            );
//        }
        FormatType formatType = getFormatType(request);
        model.setView(formatType.getViewName());
        model.setContentType(formatType.getContentType());
        // add the model elements
        model.put(RESULT_MODEL_PARAM, resultModel);
        model.put(QUERY_STRING_FOR_PRESENTATION_PARAM, createQueryStringForPresentation(
                query,
                queryConstraints
        ));
        model.put(BREADCRUMBS_PARAM, queryConstraints.toBreadcrumbs(QUERY_PARAMETER, query));
        model.put(DISPLAY_PARAMETER, formatType.getDisplay());
        model.put(QUERY_PARAMETER, query);
        model.put(NEXT_QUERY_FACETS_PARAM, nextQueryFacets);
        model.put(PAGINATION_PARAM, resultPagination);
        model.put(QUERY_TO_SAVE, request.getQueryString());
        model.put(SERVLET_URL_PARAM, ControllerUtil.getServletUrl(request));
    }

    private FormatType getFormatType(HttpServletRequest request) {
        String formatString = request.getParameter(FORMAT_PARAMETER);
        if (formatString != null) {
            return FormatType.get(formatString);
        }
        return (FormatType) request.getAttribute(FORMAT_PARAMETER);
    }

    private String getQuery(HttpServletRequest request) {
        String query = ControllerUtil.getParameter(request, QUERY_PARAMETER);
        if (query == null) {
            StringBuilder out = new StringBuilder();
            for (int count = 1; ControllerUtil.getParameter(request, QUERY_PARAMETER + count) != null; count++) {
                String q = ControllerUtil.getParameter(request, String.format("%s%d", QUERY_PARAMETER, count));
                String f = ControllerUtil.getParameter(request, String.format("%s%d", FACET_PARAMETER, count));
                String term = getFacetTerm(f, q);
                if (count > 1) {
                    String operator = ControllerUtil.getParameter(request, String.format("%s%d", OPERATOR_PARAMETER, count));
                    out.append(" ").append(operator).append(" ").append(term);
                }
                else {
                    out.append(term);
                }
            }
            if (out.length() > 0) {
                query = out.toString();
            }
        }
        return query;
    }

    private String getFacetTerm(String facet, String query) {
        if (facet != null) {
            return facet + ":" + query;
        }
        else {
            return query;
        }
    }

    private String createQueryStringForPresentation(String query, QueryConstraints queryConstraints) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        queryString.append(QUERY_PARAMETER).append("=").append(URLEncoder.encode(query, "utf-8"));
        queryString.append(queryConstraints.toQueryString());
        return queryString.toString();
    }

}