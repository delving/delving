package eu.europeana.controller;

import eu.europeana.FreemarkerUtil;
import eu.europeana.controller.util.ResultPagination;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ResultPaginationTest {

    @Test
    public void testSmall() throws Exception {
        String page = makePage(30, 20, 1);
        System.out.println(page);
    }

    @Test
    public void testStart () throws Exception {
        String page = makePage(300, 20, 1);
        System.out.println(page);
    }

    @Test
    public void testMiddle () throws Exception {
        String page = makePage(300, 20, 141);
        System.out.println(page);
    }

    @Test
    public void testNearEnd () throws Exception {
        String page = makePage(300, 20, 261);
        System.out.println(page);
    }

    @Test
    public void testEnd () throws Exception {
        String page = makePage(300, 20, 281);
        System.out.println(page);
    }

    private String makePage(int numFound, int rows, int start) throws Exception {
        System.out.println("numFound="+numFound+", rows="+rows+", start="+start);
        return makePage(new ResultPagination(numFound, rows, start));
    }

    private String makePage(ResultPagination p) throws Exception {
        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("pagination",p);
        return FreemarkerUtil.processResource("/example-pagination.ftl", model);
    }

}
