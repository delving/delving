package eu.europeana.sip.schema;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

/**
 * Just for reading config information
 * 
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("europeana-mapping-schema")
public class MappingSchema {

    @XStreamAlias("source-selection")
    public List<SourceSelection> sourceSelections;
}