package eu.europeana.normalizer;

import eu.europeana.query.RecordField;
import javax.xml.namespace.QName;

import java.util.ArrayList;

/**
 * Store a key to a field for Solr
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SolrField {
    private QName qname;
    private String language;
    private Profile.FieldMapping fieldMapping;
    private StringBuilder value = new StringBuilder();

    public SolrField(String key, RecordField mapToKey, String mapToValue) {
        this.qname = QNameBuilder.createQName(key);
        fieldMapping = new Profile.FieldMapping();
        fieldMapping.mapTo = new ArrayList<Profile.MapTo>();
        Profile.MapTo mapTo = new Profile.MapTo();
        mapTo.key = mapToKey;
        fieldMapping.mapTo.add(mapTo);
        value.append(mapToValue);
    }

    public SolrField(String key) {
        this.qname = QNameBuilder.createQName(key);
    }

    public SolrField(QName qname, Profile.FieldMapping fieldMapping) {
        this.qname = qname;
        this.fieldMapping = fieldMapping;
    }

    public SolrField(SolrField solrField) {
        this.qname = solrField.getQName();
        fieldMapping = new Profile.FieldMapping();
        fieldMapping.mapTo = new ArrayList<Profile.MapTo>();
        Profile.MapTo mt = new Profile.MapTo();
        mt.key = solrField.getKey(0);
        fieldMapping.mapTo.add(mt);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public QName getQName() {
        return qname;
    }

    public StringBuilder getValue() {
        return value;
    }

    public void setFieldMapping(Profile.FieldMapping fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public boolean hasFieldMapping() {
        return fieldMapping != null;
    }

    public boolean isChooseFirst() {
        return fieldMapping.chooseFirst;
    }

    public boolean isChooseLast() {
        return fieldMapping.chooseLast;
    }

    public Profile.FieldMapping getFieldMapping() {
        return fieldMapping;
    }

    public int getMappingCount() {
        if (fieldMapping == null || fieldMapping.mapTo == null) {
            return 0;
        }
        else {
            return fieldMapping.mapTo.size();
        }
    }

    public RecordField getKey(int index) {
        return fieldMapping.mapTo.get(index).key;
    }

    public int getConcatenateOrder(int index) {
        return fieldMapping.mapTo.get(index).order;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolrField solrField = (SolrField) o;
        return qname.equals(solrField.qname);
    }

    public int hashCode() {
        return qname.hashCode();
    }

    public String toString() {
        return qname.toString();
    }
}
