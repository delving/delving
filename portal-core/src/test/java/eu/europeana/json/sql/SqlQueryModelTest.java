package eu.europeana.json.sql;

import eu.europeana.json.JsonQueryModelFactory;
import eu.europeana.json.solr.SolrQueryModelFactory;
import eu.europeana.query.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 11, 2009: 9:33:00 AM
 */
public class SqlQueryModelTest {

    static JsonQueryModelFactory queryModelFactory;

    @BeforeClass
    public static void init() {
        queryModelFactory = new JsonQueryModelFactory();
    }

    @Before
    public void before() {
        SqlQueryModelFactory sqlQueryModelFactory = new SqlQueryModelFactory();
        sqlQueryModelFactory.setBaseUrl("http://nmisforge.isti.cnr.it:9090/backend/ObjectBridgeServlet"); //TODO
        queryModelFactory.setAdvancedQueryModelFactory(sqlQueryModelFactory);
    }

    @Test
    public void testSimpleQuery() throws EuropeanaQueryException {
        QueryModel queryModel =  queryModelFactory.createQueryModel(QueryModelFactory.SearchType.ADVANCED);
        queryModel.setResponseType(ResponseType.SMALL_BRIEF_DOC_WINDOW);
        queryModel.setRows(12);
        queryModel.setStartRow(1);
        queryModel.setQueryString("Title:max");
        ResultModel resultModel = queryModel.fetchResult();
        Assert.assertNotNull(resultModel);
//        resultModel.getBriefDocWindow().getDocs().size();
    }

    @Test
    public void testComplexQuery() throws EuropeanaQueryException {
        QueryModel queryModel =  queryModelFactory.createQueryModel(QueryModelFactory.SearchType.ADVANCED);
        queryModel.setResponseType(ResponseType.SMALL_BRIEF_DOC_WINDOW);
        queryModel.setRows(12);
        queryModel.setStartRow(1);
        queryModel.setQueryString("Title:europe AND TYPE:IMAGE");
        ResultModel resultModel = queryModel.fetchResult();
        Assert.assertNotNull(resultModel);
//        resultModel.getBriefDocWindow().getDocs().size();
    }

    @Test
    public void testSolrQuery() throws EuropeanaQueryException {
        SolrQueryModelFactory solrQueryModelFactory = new SolrQueryModelFactory();
        solrQueryModelFactory.setBaseUrl("http://europeana.siebinga.org/solr/select"); //TODO
        queryModelFactory.setAdvancedQueryModelFactory( solrQueryModelFactory);
        QueryModel queryModel =  queryModelFactory.createQueryModel(QueryModelFactory.SearchType.ADVANCED);
        queryModel.setResponseType(ResponseType.SMALL_BRIEF_DOC_WINDOW);
        queryModel.setRows(12);
        queryModel.setStartRow(1);
        queryModel.setQueryString("dc_title:max");
        ResultModel resultModel = queryModel.fetchResult();
        Assert.assertNotNull(resultModel);
//        resultModel.getBriefDocWindow().getDocs().size();
    }
}
