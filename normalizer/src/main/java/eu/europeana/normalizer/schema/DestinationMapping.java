package eu.europeana.normalizer.schema;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import eu.europeana.normalizer.transform.Transform;
import eu.europeana.query.RecordField;

/**
 * Map to the range from the domain containing this range
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("destination-mapping")
public class DestinationMapping {

    public DestinationMapping() {
    }

    public DestinationMapping(RecordField key) {
        this.key = key;
    }

    @XStreamAsAttribute
    @XStreamConverter(EnumConverter.class)
    RecordField key;

    @XStreamAsAttribute
    @XStreamConverter(EnumConverter.class)
    Transform transform;

    @XStreamAsAttribute
    int order;

    @XStreamAsAttribute
    String concatenateSuffix;

    public String toString() {
        return key.toString();
    }
}

