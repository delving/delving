package eu.europeana.json.sql;

import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import org.apache.commons.httpclient.HttpClient;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SqlQueryModelFactory implements QueryModelFactory {

    private String baseUrl;
    private HttpClient httpClient;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public QueryModel createQueryModel(SearchType searchType) {
        SqlQueryModel queryModel = new SqlQueryModel();
        queryModel.setBaseUrlList(baseUrl);
        queryModel.setHttpClient(httpClient);
        return queryModel;
    }
}