package eu.europeana.sip.schema;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import eu.europeana.sip.reference.RecordField;
import eu.europeana.sip.reference.Transform;

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

