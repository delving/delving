package eu.europeana.sip.xml;

import groovy.util.Node;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Make sure the parser is working nicely
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestNormalizationParser {

    @Test
    public void iterate() throws FileNotFoundException, XMLStreamException {
        FileInputStream in = new FileInputStream("/Users/fluxe/europeana/trunk/core/src/test/sample-metadata/92001_Ag_EU_TELtreasures.xml");
        QName recordRoot = QNameBuilder.createQName("record");
        NormalizationParser parser = new NormalizationParser(in, recordRoot);
        for (Node node : parser) {
            printNode(node, 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void printNode(Node node, int depth) {
        for (int walk=0; walk<depth; walk++) {
            System.out.print('\t');
        }
        System.out.print(node.name());
        if (node.value() instanceof List) {
            System.out.println();
            List<Node> children = (List<Node>)node.value();
            for (Node child : children) {
                printNode(child, depth+1);
            }
        }
        else {
            String string = (String)node.value();
            System.out.println(" '"+string+"'");
        }
    }

}
