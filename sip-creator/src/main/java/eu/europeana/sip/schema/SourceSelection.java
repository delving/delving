package eu.europeana.sip.schema;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Map a field from something to one or more other things
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("domain")
public class SourceSelection implements Comparable<SourceSelection> {

    public SourceSelection() {
    }

    public SourceSelection(String from) {
        this.from = from;
        this.destinationMapping = new ArrayList<DestinationMapping>();
    }

    @XStreamAsAttribute
    public String from;

    @XStreamAsAttribute
    String acceptField;

    @XStreamAsAttribute
    String discardRecord;

    @XStreamAsAttribute
    boolean chooseFirst;

    @XStreamAsAttribute
    boolean chooseLast;

    @XStreamImplicit
    public List<DestinationMapping> destinationMapping;

    @Override
    public int compareTo(SourceSelection o) {
        return from.compareTo(o.from);
    }
}

