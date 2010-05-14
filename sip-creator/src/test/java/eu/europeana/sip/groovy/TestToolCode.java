package eu.europeana.sip.groovy;

import eu.europeana.sip.model.ToolCodeModel;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Make sure the groovy code is working as expected
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class TestToolCode {

    private GroovyShell groovyShell = new GroovyShell();
    private ToolCodeModel toolCodeModel = new ToolCodeModel();

    private static final String[][] DATES = {
            {"1990", "1990"},
            {"1940-1945", "1940, 1945"},
            {"1940-45", "1940, 1945"},
            {"100 AD", "100 AD"},
            {"100 BC", "100 BC"},
            {"10/02/2010", "2010"},
            {"10/02/2010-11/02/2010", "2010"},
            {"The object was created in 1910 and sold May 2005", "1910, 2005"},
            {"2010-03-19", "2010"},
            {"2005-03-09 to 2010-03-19", "2005, 2010"},
            {"65 BC", "65 BC"},
            {"10 AD", "10 AD"},
            {"100", "100 AD"},
            {"65", "65 AD"},
    };

    @Test
    public void testDates() {
        Script script = groovyShell.parse(toolCodeModel.getToolCode());
        for (String [] d : DATES) {
            String result = (String)script.invokeMethod("extractYear", d[0]);
            Assert.assertEquals("failing to extract \""+d[0]+"\"", d[1], result);
            System.out.println("Successful: "+d[0]+" --> "+d[1]);
        }
    }
}
