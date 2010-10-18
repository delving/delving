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
        InputStream modelInputStream = getClass().getResource("/metadata-model.xml").openStream();
        InputStream mappingInputStream = getClass().getResource("/metadata-mapping.xml").openStream();
        MetaModel model = MetaModel.read(modelInputStream);
        MetaMapping mapping = MetaMapping.read(mappingInputStream);
        log.info("Code:\n" + mapping.generateCode(model));
    }
}
