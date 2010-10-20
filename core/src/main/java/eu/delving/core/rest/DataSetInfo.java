package eu.delving.core.rest;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("dataset")
public class DataSetInfo {
    public String spec;
    public String name;
    public String state;
    public Integer recordsIndexed;
    public Integer recordCount;
    public String errorMessage;
}
