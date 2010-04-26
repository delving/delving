package eu.europeana.sip.groovy;

import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import java.io.Writer;

/**
 * Create a binding that we can use to execute snippets, with record that we can update.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MappingScriptBinding extends Binding {
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String DC = "dc";
    private static final String DCTERMS = "dcterms";
    private static final String EUROPEANA = "europeana";

    public MappingScriptBinding(Writer writer) {
        MarkupBuilder builder = new MarkupBuilder(writer);
        NamespaceBuilder xmlns = new NamespaceBuilder(builder);
        setVariable(OUTPUT, builder);
        setVariable(DC, xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
        setVariable(DCTERMS, xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
        setVariable(EUROPEANA, xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
    }

    public void setRecord(GroovyNode record) {
        setVariable(INPUT, record);
    }
}
