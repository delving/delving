package eu.europeana.sip;

import eu.europeana.query.Language;
import eu.europeana.query.RecordField;
import eu.europeana.sip.converters.Converter;
import eu.europeana.sip.converters.ConverterException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * Build up the contents of one record and then render it.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SolrRecord {
    private static final ConcatenateOrderComparator CONCATENATE_ORDER_COMPARATOR = new ConcatenateOrderComparator();
    private Map<String, String> typeMapping;
    private Set<String> missingTypeMappings;
    private Map<String, String> languageMapping;
    private Set<String> missingLanguageMappings;
    private String collectionId;
    private List<SolrField> fields = new ArrayList<SolrField>();
    private List<Entry> entries = new ArrayList<Entry>();

    public SolrRecord(Map<String, String> typeMapping, Set<String> missingTypeMappings, Map<String, String> languageMapping, Set<String> missingLanguageMappings, String collectionid) {
        this.typeMapping = typeMapping;
        this.missingTypeMappings = missingTypeMappings;
        this.languageMapping = languageMapping;
        this.missingLanguageMappings = missingLanguageMappings;
        this.collectionId = collectionid;
    }

    public void render(XMLStreamWriter writer, boolean writeSolr) throws XMLStreamException, ConverterException {
        writer.writeCharacters("\t");
        if (!writeSolr) {
            writer.writeStartElement("record");
        }
        else {
            writer.writeStartElement("doc");
        }
        writer.writeCharacters("\n");
        for (Entry entry : entries) {
            writeEntry(writer, entry, writeSolr);
        }
        writer.writeCharacters("\t");
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    public void doConversions() throws ConverterException {
        for (SolrField field : fields) {
            convertValues(field);
        }
    }

    public void convertValues(SolrField field) throws ConverterException {
        String originalValue = field.getValue().toString();
        if (field.getFieldMapping().mapTo != null) {
            for (Profile.MapTo mapTo : field.getFieldMapping().mapTo) {
                Converter fieldConverter = mapTo.getConverter();
                String value = originalValue;
                if (fieldConverter != null) {
                    value = fieldConverter.convertValue(value);
                }
                if (value.indexOf('|') >= 0) {
                    String[] multiple = value.split("\\|");
                    for (String part : multiple) {
                        putMappedValue(mapTo, part, field.getLanguage());
                    }
                }
                else {
                    putMappedValue(mapTo, value, field.getLanguage());
                }
            }
        }
    }

    private void putMappedValue(Profile.MapTo mapTo, String value, String language) {
        switch (mapTo.key) {
            case EUROPEANA_TYPE:
                if (typeMapping.containsKey(value)) {
                    value = typeMapping.get(value);
                    putValue(mapTo, value, language);
                }
                else {
                    missingTypeMappings.add(value);
                }
                break;
            // dc_language should remain as is.
            case EUROPEANA_LANGUAGE:
                String mappedLanguage = languageMapping.get(value.toLowerCase());
                if (mappedLanguage != null) {
                    putValue(mapTo, mappedLanguage, language);
                }
                else {
                    Language languageObject = Language.get(value.toLowerCase(), false);
                    if (languageObject != null) {
                        putValue(mapTo, languageObject.getCode(), null);
                    }
                    else {
                        missingLanguageMappings.add(value);
                    }
                }
                break;
            case EUROPEANA_URI:
                putValue(mapTo, EuropeanaUriHasher.createEuropeanaUri(collectionId, value), language);
                break;
            default:
                putValue(mapTo, value, language);
                break;
        }
    }

    private void putValue(Profile.MapTo mapTo, String value, String language) {
        entries.add(new Entry(mapTo, value, language));
    }

    public void putValue(Profile.MapTo mapTo, String value) {
        putValue(mapTo, value, null);
    }

    private void writeEntry(XMLStreamWriter writer, Entry entry, Boolean writeSolr) throws XMLStreamException {
        QName qname = QNameBuilder.createQName(entry.getMapTo().key.toString());
        writer.writeCharacters("\t\t");
        if (writeSolr) {
            writer.writeStartElement("field");
            for (RecordField field : RecordField.values()) {
                if (field.getPrefix().equals(qname.getPrefix()) && field.getLocalName().equals(qname.getLocalPart())) {
                    if (field.getFacetType() != null) {
                        writer.writeAttribute("name", field.getFacetType().toString());
                    }
                    else {
                        writer.writeAttribute("name", field.toFieldNameString());
                    }
                    break;
                }
            }
        }
        else {
            writer.writeStartElement(qname.getPrefix(), qname.getLocalPart(), qname.getNamespaceURI());
        }
        if (entry.getLanguage() != null) {
            writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", entry.getLanguage());
        }
        writer.writeCharacters(entry.getValue());
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    public String getFirstValue(RecordField recordField) throws ConverterException {
        for (Entry entry : entries) {
            if (entry.getMapTo().key == recordField) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void setValue(RecordField recordField, String value) throws ConverterException {
        for (Entry entry : entries) {
            if (entry.getMapTo().key == recordField) {
                entry.setValue(value);
            }
        }
    }

    public boolean hasField(RecordField recordField) throws ConverterException {
        return getFirstValue(recordField) != null;
    }

    public void removeEmpty() {
        Iterator<Entry> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
            Entry entry = entryIterator.next();
            if (entry.getValue().trim().length() == 0) {
                entryIterator.remove();
            }
        }
    }

    public boolean removeIfNotURL(RecordField recordField) {
        boolean removal = false;
        Iterator<Entry> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
            Entry entry = entryIterator.next();
            if (entry.getMapTo().key != recordField) continue;
            if (!(entry.getValue().startsWith("https://") || entry.getValue().startsWith("http://")) || entry.getValue().startsWith("mms://")) {
                entryIterator.remove();
                removal = true;
            }
        }
        return removal;
    }

    public void removeDuplicates() {
        Set<String> set = new HashSet<String>();
        Iterator<Entry> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
            Entry entry = entryIterator.next();
            String entryString = entry.getMapTo().key + "|" + entry.getValue();
            if (set.contains(entryString)) {
                entryIterator.remove();
            }
            else {
                set.add(entryString);
            }
        }
    }

    public void chooseFirstOrLast() {
        Map<Profile.FieldMapping, List<SolrField>> duplicateMap = new TreeMap<Profile.FieldMapping, List<SolrField>>();
        Iterator<SolrField> iterator = fields.iterator();
        while (iterator.hasNext()) { // remove lists
            SolrField field = iterator.next();
            if (field.hasFieldMapping()) {
                if (field.isChooseFirst() || field.isChooseLast()) {
                    List<SolrField> duplicates = duplicateMap.get(field.getFieldMapping());
                    if (duplicates == null) {
                        duplicateMap.put(field.getFieldMapping(), duplicates = new ArrayList<SolrField>());
                    }
                    duplicates.add(field);
                    iterator.remove();
                }
            }
        }
        for (Map.Entry<Profile.FieldMapping, List<SolrField>> entry : duplicateMap.entrySet()) { // add members of lists
            if (entry.getKey().chooseFirst) {
                fields.add(entry.getValue().get(0));
            }
            else if (entry.getKey().chooseLast) {
                fields.add(entry.getValue().get(entry.getValue().size() - 1));
            }
        }
    }

    public void concatenateDuplicates() {
        Map<RecordField, List<Entry>> duplicateMap = new TreeMap<RecordField, List<Entry>>();
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) { // remove lists
            Entry entry = iterator.next();
            if (entry.getMapTo().order > 0) {
                List<Entry> duplicates = duplicateMap.get(entry.getMapTo().key);
                if (duplicates == null) {
                    duplicateMap.put(entry.getMapTo().key, duplicates = new ArrayList<Entry>());
                }
                duplicates.add(entry);
                iterator.remove();
            }
        }
        if (!duplicateMap.isEmpty()) {
            for (List<Entry> duplicates : duplicateMap.values()) {
                Collections.sort(duplicates, CONCATENATE_ORDER_COMPARATOR);
                StringBuilder concat = new StringBuilder();
                int countdown = duplicates.size();
                for (Entry entry : duplicates) {
                    concat.append(entry.getValue());
                    if (--countdown > 0) {
                        String concatenateSuffix = entry.getMapTo().concatenateSuffix;
                        if (concatenateSuffix == null) {
                            concatenateSuffix = " ";
                        }
                        concat.append(concatenateSuffix);
                    }
                }
                entries.add(new Entry(duplicates.get(0).mapTo, concat.toString(), duplicates.get(0).language));
            }
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("record{\n");
        for (Entry entry : entries) {
            out.append('\t').append(entry.getMapTo().key).append(" ==> [").append(entry.getValue()).append("]\n");
        }
        out.append("}\n");
        return out.toString();
    }

    public SolrField add(SolrField field) {
        fields.add(field);
        return field;
    }

    public boolean containsRecordField(RecordField recordField) {
        for (Entry entry : entries) {
            if (entry.getMapTo().key == recordField) {
                return true;
            }
        }
        return false;
    }

    public List<Entry> getEntries(RecordField recordField) {
        List<Entry> entries = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (entry.getMapTo().key == recordField) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);
    }

    public static class Entry {
        private Profile.MapTo mapTo;
        private String value;
        private String language;

        public Entry(Profile.MapTo mapTo, String value, String language) {
            this.mapTo = mapTo;
            this.value = value;
            this.language = language;
        }

        public String getValue() {
            return value;
        }

        public Profile.MapTo getMapTo() {
            return mapTo;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLanguage() {
            return language;
        }

        public String toString() {
            return mapTo.key + " => " + value;
        }
    }

    private static class ConcatenateOrderComparator implements Comparator<Entry> {
        public int compare(Entry entry0, Entry entry1) {
            return getOrder(entry0) - getOrder(entry1);
        }

        private int getOrder(Entry entry) {
            return entry.getMapTo().order;
        }
    }

}
