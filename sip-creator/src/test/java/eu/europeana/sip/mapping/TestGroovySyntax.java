package eu.europeana.sip.mapping;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Testing if the provided code is valid Groovy code
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class TestGroovySyntax {

    private GroovyShell groovyShell;

    private final String GROOVY_SYNTAX = "println(\"->${name}<-\");";

    @Before
    public void setUp() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "serkan");
        Binding binding = new Binding(map);
        groovyShell = new GroovyShell(binding);
    }

    @Test
    public void testSyntaxChecking() {
        try {
            groovyShell.evaluate(GROOVY_SYNTAX);
        }
        catch (CompilationFailedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
    }
}
