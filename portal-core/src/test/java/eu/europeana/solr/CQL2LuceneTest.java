package eu.europeana.solr;

import eu.europeana.controller.util.CQL2Lucene;
import org.junit.Test;
import org.osuosl.srw.SRWDiagnostic;
import org.z3950.zing.cql.CQLParseException;

import java.io.IOException;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class CQL2LuceneTest {

    private static final String[] CQL_QUERIES = new String[]{
            "cultural",
            "subject all \"cultulal heritage\"",
            "title = \"cultural heritage\""
    };

    @Test
    public void testCQL2Solr() throws IOException, CQLParseException, SRWDiagnostic {
        for (String cqlQuery : CQL_QUERIES) {
            System.out.println(CQL2Lucene.translate(cqlQuery));
        }
    }

}
