package eu.delving.core.metadata;

import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.InputStream;

/**
 * Write and read for now
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMetadataModel {
    private Logger log = Logger.getLogger(getClass());

    @Test
    public void writeAndRead() throws Exception {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[] {
                MetadataModel.class,
                MetadataNode.class,
                MetadataField.class 
        });
        InputStream in = getClass().getResource("/metadata-model.xml").openStream();
        MetadataModel metadataModel = (MetadataModel)stream.fromXML(in);
        log.info("MetadataModel:\n\n" + metadataModel);
    }

}
