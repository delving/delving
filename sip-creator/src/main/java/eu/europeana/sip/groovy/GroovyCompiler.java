package eu.europeana.sip.groovy;

import groovy.util.Node;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public interface GroovyCompiler {

    Class compile(String code);

    Class compile(Node node);
}
