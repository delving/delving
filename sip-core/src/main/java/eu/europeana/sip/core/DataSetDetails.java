package eu.europeana.sip.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The extra data required when uploading a zip to the repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("dataset-details")
public class DataSetDetails {
    private String spec;
    private String name;
    private String providerName;
    private String description;
    private String namespace;
    private String schema;
    private String recordRoot;
    private String uniqueElement;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getRecordRoot() {
        return recordRoot;
    }

    public void setRecordRoot(String recordRoot) {
        this.recordRoot = recordRoot;
    }

    public String getUniqueElement() {
        return uniqueElement;
    }

    public void setUniqueElement(String uniqueElement) {
        this.uniqueElement = uniqueElement;
    }

}
