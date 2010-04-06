package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner


/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class ResultPaginationSpec extends Spec with ShouldMatchers {



/*
  @Test
    public void smallPagination() throws Exception {
        String page = makePage(30, 20, 0);
        assertEquals(
                "1\n" +
                        "2(21)\n" +
                        "next(21)\n",
                page
        );
    }

    @Test
    public void startPagination() throws Exception {
        String page = makePage(300, 20, 0);
        assertEquals(
                "1\n" +
                        "2(21)\n" +
                        "3(41)\n" +
                        "4(61)\n" +
                        "5(81)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8(141)\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "next(21)\n",
                page
        );
    }

    @Test
    public void middlePagination() throws Exception {
        String page = makePage(300, 20, 140);
        assertEquals(
                "prev(121)\n" +
                        "3(41)\n" +
                        "4(61)\n" +
                        "5(81)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "11(201)\n" +
                        "12(221)\n" +
                        "next(161)\n",
                page
        );
    }

    @Test
    public void nearEndPagination() throws Exception {
        String page = makePage(300, 20, 260);
        assertEquals(
                "prev(241)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8(141)\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "11(201)\n" +
                        "12(221)\n" +
                        "13(241)\n" +
                        "14\n" +
                        "15(281)\n" +
                        "next(281)\n",
                page
        );

    }

    @Test
    public void endPagination() throws Exception {
        String page = makePage(300, 20, 280);
        assertEquals(
                "prev(261)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8(141)\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "11(201)\n" +
                        "12(221)\n" +
                        "13(241)\n" +
                        "14(261)\n" +
                        "15\n",
                page
        );
    }

    private String makePage(int numFound, int rows, int start) throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("");
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        return makePage(new ResultPaginationImpl(solrQuery, numFound, "queryString??"));
    }

    private String makePage(ResultPaginationImpl p) throws Exception {
        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("pagination", p);
        return FreemarkerUtil.processResource("/example-pagination.ftl", model);
    }*/

  /*
    def processResource(templateName: String, model: Map[_, _]): String = {
  var out: StringWriter = new StringWriter
getResourceTemplate(templateName).process(model, out)
return out.toString
}



  def processWebInf(templateName: String, model: Map[_, _]): String = {
  var out: StringWriter = new StringWriter
getWebInfTemplate(templateName).process(model, out)
return out.toString
}



private   def getResourceTemplate(fileName: String): Template = {
return getTemplate(fileName, new InputStreamReader(classOf[FreemarkerUtil].getResourceAsStream(fileName)))
}



  def getWebInfTemplate(fileName: String): Template = {
return getTemplate(fileName, new FileReader("./portal-full/src/main/webapp/WEB-INF/templates/" + fileName))
}



private   def getTemplate(name: String, reader: Reader): Template = {
  var configuration: Configuration = new Configuration
configuration.setLocale(new Locale("nl"))
configuration.setObjectWrapper(new DefaultObjectWrapper)
return new Template(name, reader, configuration)
}
   */
}