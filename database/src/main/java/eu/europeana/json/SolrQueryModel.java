package eu.europeana.json;

import eu.europeana.query.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SolrQueryModel implements QueryModel {

    private Logger log = Logger.getLogger(getClass());



    private HttpClient httpClient;
    private String solrBaseUrl;
    private ResponseType responseType;
    private QueryModel.Constraints constraints;
    private String queryString = "*:*";
    private QueryExpression.QueryType queryType = QueryExpression.QueryType.SIMPLE_QUERY;
    private String facetLimit = "100";
    private String facetMinCount = "1";
    private boolean moreLikeThis;
    private boolean facets;
    private int startRow;
    private int rows = 0;
    private RecordFieldChoice recordFieldChoice;

    @Deprecated
    public void setSolrBaseUrl(String solrBaseUrl) {
        this.solrBaseUrl = solrBaseUrl;
    }

    @Deprecated
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Deprecated
    public void setFacetLimit(String facetLimit) {
        this.facetLimit = facetLimit;
    }

    @Deprecated
    public void setFacetMinCount(String facetMinCount) {
        this.facetMinCount = facetMinCount;
    }

    @Deprecated
    public QueryExpression setQueryString(String queryString) throws EuropeanaQueryException {
        QueryExpression queryExpression = new SolrQueryExpression(queryString);
        setQueryExpression(queryExpression);
        return queryExpression;
    }

    @Deprecated
    public void setQueryExpression(QueryExpression queryExpression) {
        this.queryString = queryExpression.getBackendQueryString();
        this.queryType = queryExpression.getType();
        this.moreLikeThis = queryExpression.isMoreLikeThis();
    }

    @Deprecated
    public void setQueryConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    @Deprecated
    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
        this.rows = responseType.getRows();
        this.facets = responseType.isFacets();
        switch (responseType) {
            case SINGLE_FULL_DOC:
                recordFieldChoice = RecordFieldChoice.FULL_DOC;
                break;
            case FACETS_ONLY:
                recordFieldChoice = RecordFieldChoice.DOC_ID; // maybe could be empty?
                break;
            case SMALL_BRIEF_DOC_WINDOW:
                recordFieldChoice = RecordFieldChoice.BRIEF_DOC;
                break;
            case LARGE_BRIEF_DOC_WINDOW:
                recordFieldChoice = RecordFieldChoice.BRIEF_DOC;
                break;
            case DOC_ID_WINDOW:
                recordFieldChoice = RecordFieldChoice.DOC_ID;
                break;
        }
    }

    @Deprecated
    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    @Deprecated
    public int getStartRow() {
        return startRow;
    }

    @Deprecated
    public void setRows(int rows) {
        this.rows = rows;
    }

    @Deprecated
    public int getRows() {
        return rows;
    }

    @Deprecated
    public ResultModel fetchResult() throws EuropeanaQueryException {
        Exception firstException = null;
        int attempt = 0;
        while (attempt < 5) {
            GetMethod method = null;
            try {
                method = new GetMethod(solrBaseUrl);
                method.setQueryString(createRequestParameters());
                log.debug(method.getQueryString());
                httpClient.executeMethod(method);
                if (method.getStatusCode() == HttpStatus.SC_OK) {
                    String responseString = method.getResponseBodyAsString();
                    log.debug(responseString);
                    return new JsonResultModel(responseString, responseType);
                }
                else if (method.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    log.warn("Request to " + solrBaseUrl + " returned HTTP error " + method.getStatusCode() + ": " + method.getStatusText());
                    String errorMessage = method.getStatusText();
                    if (!errorMessage.startsWith("undefined field")) {
                        //"undefined field" means user provided invalid query.
                        //there might be more user input errors.
                        errorMessage = ""; //Hide possibly confusing error messages.
                    }
                    return new JsonResultModel(null, responseType, true, errorMessage);
                }
            }
            catch (IOException e) {
                log.error("Unable to fetch result", e);
                if (firstException == null) { // todo: this is always null. maybe the exception here shuld not be thrown until after retries
                    firstException = e;
                }
                throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), firstException);
            }
            catch (JSONException e) {
                log.error("Unable to fetch result", e);
                if (firstException == null) {
                    firstException = e;
                }
                throw new EuropeanaQueryException(QueryProblem.UNABLE_TO_PARSE_JSON.toString(), firstException);
            }
            finally {
                // because we use multithreaded connection manager the connection needs to be released manually
                if (method != null) {
                    method.releaseConnection();
                }
            }
            attempt++;
        }
        throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), firstException);
    }

    @Deprecated
    private NameValuePair[] createRequestParameters() {
        NameValueList list = new NameValueList();
        list.put("q", queryString);
        list.put("wt", "json");
        list.put("indent", "false");
        list.put("qt", queryType.toString());
        list.put("start", String.valueOf(startRow));
        list.put("rows", String.valueOf(rows));
        if (constraints != null) {
            for (FacetType facetType : constraints.getFacetTypes()) {
                list.put("fq", buildFilterString(facetType));
            }
        }
        if (facets) {
            list.put("facet", "true");
            list.put("facet.mincount", facetMinCount);
            list.put("facet.limit", facetLimit);
            for (FacetType field : FacetType.values()) {
                if (field.isSearchable()) {
                    list.put("facet.field", String.format("{!ex=%s}%s", field.getTagName(), field.toString()));
                }
            }
        }
        if (moreLikeThis) {
            list.put("mlt", "true");
        }
        if (queryType == QueryExpression.QueryType.MORE_LIKE_THIS_QUERY || moreLikeThis) {
            list.put("mlt.fl", "title,description,what,when,who");
            list.put("mlt.minwl", "3");
            list.put("mlt.maxwl", "15");
        }
        list.put("fl", toCommaDelimited(recordFieldChoice.getRecordFields()));
        return list.getArray();
    }

    @Deprecated
    String buildFilterString(FacetType facetType) {
        StringBuilder out = new StringBuilder();
//            out.append(' ');
        List<String> values = constraints.getConstraint(facetType);
        if (values.size() == 1) {
            for (String value : values) {
                // add !tag to exclude this fq= from the facet count
                out.append("{!tag=").append(facetType.getTagName()).append("}");
                out.append(facetType).append(":").append('"').append(value).append('"');
            }
        }
        else {
            int count = values.size();
            // add !tag to exclude this fq= from the facet count
            out.append("{!tag=").append(facetType.getTagName()).append("}");
            out.append(facetType).append(":").append("(\"");
            for (String value : values) {
                out.append(value);
                --count;
                if (count > 0) {
                    out.append("\" OR \"");
                }
            }
            out.append("\")");
        }
        return out.toString();
    }

    @Deprecated
    private static String toCommaDelimited(ArrayList<String> list) {
        StringBuilder out = new StringBuilder();
        for (String element : list) {
            out.append(",");
            out.append(element);
        }
        return out.toString().substring(1); // first comma
    }

    @Deprecated
    private static class NameValueList extends ArrayList<NameValuePair> {
        private static final long serialVersionUID = -1770113063469852797L;

        public void put(final String name, final String value) {
            this.add(new NameValuePair(name, value));
        }

        public NameValuePair[] getArray() {
            return this.toArray(new NameValuePair[this.size()]);
        }
    }

    @Deprecated
    public ResponseType getResponseType() {
        return responseType;
    }

    @Deprecated
    public Constraints getConstraints() {
        return constraints;
    }

    @Deprecated
    public String getQueryString() {
        return queryString;
    }

    @Deprecated
    public QueryExpression.QueryType getQueryType() {
        return queryType;
    }

    @Deprecated
    public RecordFieldChoice getRecordFieldChoice() {
        return recordFieldChoice;
    }


    // new solrJ implementation methods
    @Autowired
    private CommonsHttpSolrServer solrServer;

    public void setSolrServer(CommonsHttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }


    // create solr query from http query parameters
    public SolrQuery createFromQueryParams(Map<String, String[]> params) throws EuropeanaQueryException {
        SolrQuery solrQuery = new SolrQuery();
        if (!params.containsKey("query")) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString());
        }
        solrQuery.setQuery(params.get("query")[0]); // only get the first one
        if (params.containsKey("start")) {
            solrQuery.setStart(Integer.valueOf(params.get("start")[0]));
        }
        if (params.containsKey("rows")) {
            solrQuery.setRows(Integer.valueOf(params.get("rows")[0]));
        }
        solrQuery.setQueryType(findSolrQueryType(solrQuery.getQuery()));

        //set constraints
        solrQuery.setFilterQueries(params.get("qf"));


        return solrQuery;
    }

    private String findSolrQueryType(String query) {
        // todo: finish this
        QueryType queryType = null;

        return queryType.appearance;
    }

    public QueryResponse getSolrResponse(SolrQuery solrQuery) throws EuropeanaQueryException { // add bean to ???
        // set facets

        // set search fields

        //set more like this


        QueryResponse queryResponse;
        try {
            queryResponse = solrServer.query(solrQuery);
        } catch (SolrServerException e) {
            log.error("Unable to fetch result", e);
                throw new EuropeanaQueryException(QueryProblem.SOLR_UNREACHABLE.toString(), e);
        }
        return queryResponse;
    }

    public enum QueryType {
        SIMPLE_QUERY("europeana"),
        ADVANCED_QUERY("standard"),
        MORE_LIKE_THIS_QUERY("moreLikeThis");

        private String appearance;

        QueryType(String appearance) {
            this.appearance = appearance;
        }

        public String toString() {
            return appearance;
        }
    }
}
