package eu.europeana.definitions.metadata;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;

/**
 * Write and read for now
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMetadataField {

    @Test // not really a test yet
    public void writeAndRead() throws Exception {
        XStream stream = new XStream();
        stream.processAnnotations(MetadataField.class);
        String xml = stream.toXML(new MetadataField());
        System.out.println(xml);
        MetadataField metadataField = (MetadataField)stream.fromXML(xml);
        System.out.println(metadataField);

    }

}
