package eu.delving.core.metadata;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.InputStream;

/**
 * Write and read for now
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMetaModel {
    private Logger log = Logger.getLogger(getClass());

    @Test
    public void generateCode() throws Exception {
        InputStream modelInputStream = getClass().getResource("/record-definition.xml").openStream();
        InputStream mappingInputStream = getClass().getResource("/record-mapping.xml").openStream();
        RecordDefinition recordDefinition = RecordDefinition.read(modelInputStream);
        RecordMapping mapping = RecordMapping.read(mappingInputStream);
        log.info("Code:\n" + mapping.toCode(recordDefinition));
    }
}
