package eu.europeana.sip.core;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Make sure the groovy code is working as expected
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class TestToolCode {

    private GroovyCodeResource groovyCodeResource = new GroovyCodeResource();

    @Test
    public void testToId() {
        GroovyNode root = new GroovyNode(null, "nobody cares");
        new GroovyNode(root, "unique_a", "SomeUniqueString");
        new GroovyNode(root, "unique_b", "SomeUniqueStringy");
        Script script = shell().parse(
                "use (MappingCategory) {\n" +
                        "[\n" +
                        "root.unique_a.toId(spec),\n" +
                        "root.unique_b.toId(spec)\n" +
                        "]\n" +
                        "}\n"
        );
        script.getBinding().setVariable("root", root);
        script.getBinding().setVariable("spec", "EyeDee");
        List result = (List) script.run();
        Assert.assertEquals("id creation failed", "[EyeDee/E27E3D900B89515FEF09D7EC5D85768257AB055D]", result.get(0).toString());
        Assert.assertEquals("id creation failed", "[EyeDee/BDD6943859139B4A0AE966FA6B9B38F466665EEF]", result.get(1).toString());
    }

    @Test
    public void testDates() {
        String[][] cases = {
                {"1990", "[1990]"},
                {"1940-1945", "[1940, 1945]"},
                {"1940-45", "[1940, 1945]"},
                {"100 AD", "[100 AD]"},
                {"100 BC", "[100 BC]"},
                {"10/02/2010", "[2010]"},
                {"10/02/2010-11/02/2010", "[2010]"},
                {"The object was created in 1910 and sold May 2005", "[1910, 2005]"},
                {"2010-03-19", "[2010]"},
                {"2005-03-09 to 2010-03-19", "[2005, 2010]"},
                {"65 BC", "[65 BC]"},
                {"10 AD", "[10 AD]"},
                {"100", "[100 AD]"},
                {"65", "[65 AD]"},
        };
        GroovyNode root = new GroovyNode(null, "nobody cares");
        StringBuilder out = new StringBuilder("use (MappingCategory) { [\n");
        for (int walk = 0; walk < cases.length; walk++) {
            new GroovyNode(root, "case" + walk, cases[walk][0]);
            out.append(String.format("root.case%d.extractYear(),\n", walk));
        }
        out.append("] }\n");
        Script script = shell().parse(out.toString());
        script.getBinding().setVariable("root", root);
        List result = (List) script.run();
        for (int walk = 0; walk < cases.length; walk++) {
            Assert.assertEquals("mismatch", cases[walk][1], result.get(walk).toString());
        }
    }

    @Test
    public void testSanitizer() {
        GroovyNode root = new GroovyNode(null, "nobody cares");
        new GroovyNode(root, "with_apos", "entry with '        apostrophe");
        new GroovyNode(root, "with_enter", "entry with \n         enter");
        Script script = shell().parse(
                "use (MappingCategory) {\n" +
                        "[\n" +
                        "root.with_apos.sanitize(),\n" +
                        "root.with_enter.sanitize()\n" +
                        "]\n" +
                        "}\n"
        );
        script.getBinding().setVariable("root", root);
        List result = (List) script.run();
        Assert.assertEquals("mismatch", "entry with ' apostrophe", result.get(0));
        Assert.assertEquals("mismatch", "entry with enter", result.get(1));
    }

    private GroovyShell shell() {
        return groovyCodeResource.createShell();
    }
}
