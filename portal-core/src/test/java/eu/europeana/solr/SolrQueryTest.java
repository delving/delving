package eu.europeana.solr;

import eu.europeana.FreemarkerUtil;
import eu.europeana.controller.util.NextQueryFacet;
import eu.europeana.controller.util.QueryConstraints;
import eu.europeana.json.solr.SolrQueryModel;
import eu.europeana.query.ResponseType;
import eu.europeana.query.ResultModel;
import org.apache.commons.httpclient.HttpClient;
import org.junit.Test;

import java.util.HashMap;

/**
 * Test querying the solr server using json
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SolrQueryTest {
    // todo initiate solr starter programmatically
    private static final String BASE_URL = "http://localhost:8983/solr/select";

    @Test
    public void facetRequest() throws Exception {
        HashMap<String, Object> model = new HashMap<String, Object>();
        QueryConstraints queryConstraints = new QueryConstraints(new String [] {
                "YEAR:1915",
                "LANGUAGE:de"
        });
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setHttpClient(new HttpClient());
        queryModel.setSolrBaseUrl(BASE_URL);
        queryModel.setQueryString("kultur");

        queryModel.setResponseType(ResponseType.FACETS_ONLY);
        ResultModel facetResults = queryModel.fetchResult();
        model.put("facets", NextQueryFacet.createDecoratedFacets(
                facetResults.getFacets(),
                queryConstraints
        ));

        queryModel.setResponseType(ResponseType.SMALL_BRIEF_DOC_WINDOW);
        queryModel.setQueryConstraints(queryConstraints);
        ResultModel resultModel = queryModel.fetchResult();
        System.out.println(resultModel);

        String page = FreemarkerUtil.processResource("/example-facets.ftl", model);
        System.out.println(page);
    }

    @Test
    public void briefDocRequest() throws Exception {
        QueryConstraints queryConstraints = new QueryConstraints(new String [] {
                "YEAR:1915",
                "YEAR:1919",
                "LANGUAGE:de"
        });
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setHttpClient(new HttpClient());
        queryModel.setSolrBaseUrl(BASE_URL);
        queryModel.setQueryString("kultur");
        queryModel.setResponseType(ResponseType.SMALL_BRIEF_DOC_WINDOW);
        queryModel.setQueryConstraints(queryConstraints);
        ResultModel resultModel = queryModel.fetchResult();
//        System.out.println(resultModel);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("result", resultModel);
        System.out.println(FreemarkerUtil.processResource("/example-brief-doc-page.ftl", model));
        System.out.println(FreemarkerUtil.processResource("/example-rss2.ftl", model));
    }

    @Test
    public void docIdRequest() throws Exception {
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setHttpClient(new HttpClient());
        queryModel.setSolrBaseUrl(BASE_URL);
        queryModel.setResponseType(ResponseType.DOC_ID_WINDOW);
        queryModel.setQueryString("kultur");
        ResultModel resultModel = queryModel.fetchResult();
        System.out.println(resultModel);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("result", resultModel);
        String page = FreemarkerUtil.processWebInf("doc-id-window.ftl",model);
        System.out.println(page);
    }

//    @Test
//    public void fullDocRequest() throws Exception {
//        SolrQueryModel queryModel = new SolrQueryModel();
//        queryModel.setHttpClient(new HttpClient());
//        queryModel.setBaseUrlMap(BASE_URL_MAP);
//        queryModel.setResponseType(ResponseType.SINGLE_FULL_DOC);
//        queryModel.setQueryString(RecordField.EUROPEANA_URI.toFieldNameString()+":\"http://europeana.siebinga.org/resolve/record/900/84466\""); // TODO: this must change
//        ResultModel resultModel = queryModel.fetchResult();
//        System.out.println(resultModel);
//        HashMap<String, Object> model = new HashMap<String, Object>();
//        model.put("result", resultModel);
//        String page = FreemarkerUtil.processResource("/example-full-doc-page.ftl",model);
//        System.out.println(page);
//    }
}