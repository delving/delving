package eu.europeana.json.sql;

import eu.europeana.json.JsonResultModel;
import eu.europeana.query.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SqlQueryModel implements QueryModel {

    private Logger log = Logger.getLogger(getClass());
    private HttpClient httpClient;
    private String baseUrl;
    private ResponseType responseType;
    private QueryModel.Constraints constraints;
    private String queryString = "*:*";
    private QueryExpression.QueryType queryType = QueryExpression.QueryType.ADVANCED_QUERY;
    private boolean facets;
    private int startRow;
    private int rows = 0;
    private RecordFieldChoice recordFieldChoice;

    public void setBaseUrlList(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public QueryExpression setQueryString(String queryString) throws EuropeanaQueryException {
        QueryExpression queryExpression = new SqlQueryExpression(queryString);
        setQueryExpression(queryExpression);
        return queryExpression;
    }

    public void setQueryExpression(QueryExpression queryExpression) {
        this.queryString = queryExpression.getBackendQueryString();
        this.queryType = queryExpression.getType();
    }

    public void setQueryConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

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

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getRows() {
        return rows;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public Constraints getConstraints() {
        return constraints;
    }

    public String getQueryString() {
        return queryString;
    }

    public QueryExpression.QueryType getQueryType() {
        return queryType;
    }

    public RecordFieldChoice getRecordFieldChoice() {
        return recordFieldChoice;
    }

    public ResultModel fetchResult() throws EuropeanaQueryException {
        Exception firstException = null;
        int attempt = 0;
        while (attempt < 5) {
            GetMethod method = null;
            try {
                method = new GetMethod(baseUrl);
                method.setQueryString(createRequestParameters());
                log.info(method.getQueryString());
                httpClient.executeMethod(method);
                if (method.getStatusCode() == HttpStatus.SC_OK) {
                    String responseString = method.getResponseBodyAsString();
                    log.info(responseString);
                    return new JsonResultModel(responseString, responseType);
                }
                log.warn("Request to " + baseUrl + " returned HTTP error " + method.getStatusCode() + ": " + method.getStatusText());
            }
            catch (IOException e) {
                log.error("Unable to fetch result", e);
                if (firstException == null) {
                    firstException = e;
                }
            }
            catch (JSONException e) {
                log.error("Unable to fetch result", e);
                if (firstException == null) {
                    firstException = e;
                }
            }
            finally {
                // because we use multithreaded connection manager the connection needs to be released manually
                if (method != null) {
                    method.releaseConnection();
                }
            }
            attempt++;
        }
        throw new EuropeanaQueryException("Attempts exhausted", firstException);
    }

    private NameValuePair[] createRequestParameters() {
        NameValueList list = new NameValueList();
        list.put("q", queryString);
//        list.put("qt", queryType.toString());
        list.put("start", String.valueOf(startRow));
        list.put("rows", String.valueOf(rows));
        list.put("fq", buildFilterString());
//        if (facets) {
//            list.put("facet", "true");
//            list.put("facet.mincount", facetMinCount);
//            list.put("facet.limit", facetLimit);
//            for (FacetType field : FacetType.values()) {
//                if (field.isSearchable()) {
//                    list.put("facet.field", field.toString());
//                }
//            }
//        }
//        if (moreLikeThis) {
//            list.put("mlt", "true");
//        }
//        if (queryType == QueryExpression.QueryType.MORE_LIKE_THIS_QUERY || moreLikeThis) {
//            list.put("mlt.mindf","1");
//            list.put("mlt.mintf","1");
//            list.put("mlt.match.include","true");
//            list.put("mlt.interestingTerms","details"); // options: list, details, none
//            list.put("mlt.boost","false");
//            list.put("mlt.fl", "title,description,what,when,who");
//            list.put("mlt.minwl", "3");
//            list.put("mlt.maxwl", "15");
//        }
//        list.put("fl", toCommaDelimited(recordFieldChoice.getRecordFields()));
        return list.getArray();
    }

    private String buildFilterString() {
        if (constraints == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (FacetType facetType : constraints.getFacetTypes()) {
            out.append(' ');
            List<String> values = constraints.getConstraint(facetType);
            if (values.size() == 1) {
                for (String value : values) {
                    out.append(facetType).append(":").append('"').append(value).append('"');
                }
            }
            else {
                int count = values.size();
                for (String value : values) {
                    out.append(facetType).append(":").append('"').append(value).append('"');
                    if (--count > 0) {
                        out.append(" OR ");
                    }
                }
            }
        }
        return out.toString();
    }

    private static String toCommaDelimited(ArrayList<String> list) {
        StringBuilder out = new StringBuilder();
        for (String element : list) {
            out.append(",");
            out.append(element);
        }
        return out.toString().substring(1); // first comma
    }

    private static class NameValueList extends ArrayList<NameValuePair> {
        private static final long serialVersionUID = -1770113063469852797L;

        public void put(final String name, final String value) {
            this.add(new NameValuePair(name, value));
        }

        public NameValuePair[] getArray() {
            return this.toArray(new NameValuePair[this.size()]);
        }
    }

}
