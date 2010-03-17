package eu.europeana.sip.xml;

import eu.europeana.core.querymodel.annotation.EuropeanaField;
import eu.europeana.sip.reference.Profile;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Build up the contents of one record and then render it.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetadataRecord {
    private Map<String, String> typeMapping;
    private List<EuropeanaField> europeanaFields;
    private Set<String> missingTypeMappings;
    private Map<String, String> languageMapping;
    private Set<String> missingLanguageMappings;
    private String collectionId;
    private List<MetadataField> fields = new ArrayList<MetadataField>();
    private List<Entry> entries = new ArrayList<Entry>();

    public MetadataRecord(List<EuropeanaField> europeanaFields, Map<String, String> typeMapping, Set<String> missingTypeMappings, Map<String, String> languageMapping, Set<String> missingLanguageMappings, String collectionid) {
        this.europeanaFields = europeanaFields;
        this.typeMapping = typeMapping;
        this.missingTypeMappings = missingTypeMappings;
        this.languageMapping = languageMapping;
        this.missingLanguageMappings = missingLanguageMappings;
        this.collectionId = collectionid;
    }

    public void render(XMLStreamWriter writer, boolean writeSolr) throws XMLStreamException {
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

    public void doConversions() {
        for (MetadataField field : fields) {
//            convertValues(field); todo: groovy!  (somehow)
        }
    }

//    public void convertValues(MetadataField field) {
//        String originalValue = field.getValue().toString();
//        if (field.getFieldMapping().mapTo != null) {
//            for (Profile.MapTo mapTo : field.getFieldMapping().mapTo) {
//                Converter fieldConverter = mapTo.getConverter();
//                String value = originalValue;
//                if (fieldConverter != null) {
//                    value = fieldConverter.convertValue(value);
//                }
//                if (value.indexOf('|') >= 0) {
//                    String[] multiple = value.split("\\|");
//                    for (String part : multiple) {
//                        putMappedValue(mapTo, part, field.getLanguage());
//                    }
//                }
//                else {
//                    putMappedValue(mapTo, value, field.getLanguage());
//                }
//            }
//        }
//    }

    private void putMappedValue(EuropeanaField europeanaField, String value, String language) {
        if (europeanaField.isEuropeanaUri()) {
            putValue(europeanaField, EuropeanaUriHasher.createEuropeanaUri(collectionId, value), language);
        }
        else if (europeanaField.isEuropeanaType()) {
            if (typeMapping.containsKey(value)) {
                value = typeMapping.get(value);
                putValue(europeanaField, value, language);
            }
            else {
                missingTypeMappings.add(value);
            }
        }
//        else if (EUROPEANA_LANGUAGE???) {
//            String mappedLanguage = languageMapping.get(value.toLowerCase());
//            if (mappedLanguage != null) {
//                putValue(mapTo, mappedLanguage, language);
//            }
//            else {
//                Language languageObject = Language.get(value.toLowerCase(), false);
//                if (languageObject != null) {
//                    putValue(mapTo, languageObject.getCode(), null);
//                }
//                else {
//                    missingLanguageMappings.add(value);
//                }
//            }
//        }
        else {
            putValue(europeanaField, value, language);
        }
    }

    private void putValue(EuropeanaField europeanaField, String value, String language) {
        entries.add(new Entry(europeanaField, value, language));
    }

    public void putValue(EuropeanaField europeanaField, String value) {
        putValue(europeanaField, value, null);
    }

    private void writeEntry(XMLStreamWriter writer, Entry entry, Boolean writeSolr) throws XMLStreamException {
        QName qname = QNameBuilder.createQName(entry.getMapToKey());
        writer.writeCharacters("\t\t");
        if (writeSolr) {
            writer.writeStartElement("field");
            for (EuropeanaField europeanaField : europeanaFields) {
                if (europeanaField.getPrefix().equals(qname.getPrefix()) && europeanaField.getName().equals(qname.getLocalPart())) {
                    if (europeanaField.isFacet()) {
                        writer.writeAttribute("name", europeanaField.getFacetName());
                    }
                    else {
                        writer.writeAttribute("name", europeanaField.getFieldNameString());
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

    public String getFirstValue(EuropeanaField europeanaField){
        for (Entry entry : entries) {
            if (entry.getMapToKey().equals(europeanaField.getFieldNameString())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void setValue(EuropeanaField europeanaField, String value){
        for (Entry entry : entries) {
            if (entry.getMapToKey().equals(europeanaField.getFieldNameString())) {
                entry.setValue(value);
            }
        }
    }

    public boolean hasField(EuropeanaField europeanaField){
        return getFirstValue(europeanaField) != null;
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

    public boolean removeIfNotURL(EuropeanaField europeanaField) {
        boolean removal = false;
        Iterator<Entry> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
            Entry entry = entryIterator.next();
            if (!entry.getMapToKey().equals(europeanaField.getFieldNameString())) continue;
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
            String entryString = entry.getMapToKey() + "|" + entry.getValue();
            if (set.contains(entryString)) {
                entryIterator.remove();
            }
            else {
                set.add(entryString);
            }
        }
    }

    public void chooseFirstOrLast() {
        Map<Profile.FieldMapping, List<MetadataField>> duplicateMap = new TreeMap<Profile.FieldMapping, List<MetadataField>>();
        Iterator<MetadataField> iterator = fields.iterator();
        while (iterator.hasNext()) { // remove lists
            MetadataField field = iterator.next();
            if (field.hasFieldMapping()) {
                if (field.isChooseFirst() || field.isChooseLast()) {
                    List<MetadataField> duplicates = duplicateMap.get(field.getFieldMapping());
                    if (duplicates == null) {
                        duplicateMap.put(field.getFieldMapping(), duplicates = new ArrayList<MetadataField>());
                    }
                    duplicates.add(field);
                    iterator.remove();
                }
            }
        }
        for (Map.Entry<Profile.FieldMapping, List<MetadataField>> entry : duplicateMap.entrySet()) { // add members of lists
            if (entry.getKey().chooseFirst) {
                fields.add(entry.getValue().get(0));
            }
            else if (entry.getKey().chooseLast) {
                fields.add(entry.getValue().get(entry.getValue().size() - 1));
            }
        }
    }

//    public void concatenateDuplicates() { todo: revive this
//        Map<EuropeanaField, List<Entry>> duplicateMap = new TreeMap<EuropeanaField, List<Entry>>();
//        Iterator<Entry> iterator = entries.iterator();
//        while (iterator.hasNext()) { // remove lists
//            Entry entry = iterator.next();
//            if (entry.getMapToOrder() > 0) {
//                List<Entry> duplicates = duplicateMap.get(entry.getMapToKey());
//                if (duplicates == null) {
//                    duplicateMap.put(entry.getMapTo().key, duplicates = new ArrayList<Entry>());
//                }
//                duplicates.add(entry);
//                iterator.remove();
//            }
//        }
//        if (!duplicateMap.isEmpty()) {
//            for (List<Entry> duplicates : duplicateMap.values()) {
//                Collections.sort(duplicates, CONCATENATE_ORDER_COMPARATOR);
//                StringBuilder concat = new StringBuilder();
//                int countdown = duplicates.size();
//                for (Entry entry : duplicates) {
//                    concat.append(entry.getValue());
//                    if (--countdown > 0) {
//                        String concatenateSuffix = entry.getMapTo().concatenateSuffix;
//                        if (concatenateSuffix == null) {
//                            concatenateSuffix = " ";
//                        }
//                        concat.append(concatenateSuffix);
//                    }
//                }
//                entries.add(new Entry(duplicates.get(0).mapTo, concat.toString(), duplicates.get(0).language));
//            }
//        }
//    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("record{\n");
        for (Entry entry : entries) {
            out.append('\t').append(entry.getMapToKey()).append(" ==> [").append(entry.getValue()).append("]\n");
        }
        out.append("}\n");
        return out.toString();
    }

    public MetadataField add(MetadataField field) {
        fields.add(field);
        return field;
    }

    public boolean containsRecordField(EuropeanaField europeanaField) {
        for (Entry entry : entries) {
            if (entry.isForField(europeanaField)) {
                return true;
            }
        }
        return false;
    }

    public List<Entry> getEntries(EuropeanaField europeanaField) {
        List<Entry> entries = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (entry.isForField(europeanaField)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);
    }

    public static class Entry {
        private EuropeanaField europeanaField;
        private String value;
        private String language;

        public Entry(EuropeanaField europeanaField, String value, String language) {
            this.europeanaField = europeanaField;
            this.value = value;
            this.language = language;
        }

        public String getValue() {
            return value;
        }

        public boolean isForField(EuropeanaField europeanaField) {
            return europeanaField == this.europeanaField; // any better comparison?  they're the same instances from annot proc
        }

        public String getMapToKey() {
            return europeanaField.getFieldNameString(); // todo: correct?
        }

        public EuropeanaField getEuropeanaField() {
            return europeanaField;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLanguage() {
            return language;
        }

        public String toString() {
            return europeanaField.getFieldNameString() + " => " + value;
        }
    }

//    private static class ConcatenateOrderComparator implements Comparator<Entry> {
//        @Override
//        public int compare(Entry entry0, Entry entry1) {
//            return getOrder(entry0) - getOrder(entry1);
//        }
//
//        private int getOrder(Entry entry) {
//            return entry.getMapToOrder();
//        }
//    }

}
