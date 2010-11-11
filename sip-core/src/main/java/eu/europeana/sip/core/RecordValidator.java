/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.core;

import eu.delving.core.metadata.FieldDefinition;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.NamespaceDefinition;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.RecordDefinition;
import eu.delving.core.metadata.Tag;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Validate a record
 * <p/>
 * todo: move this class to the definitions module
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordValidator {
    private static final int[] PRIMES = {
            4105001, 4105019, 4105033, 4105069,
            4105091, 4105093, 4105103, 4105111,
            4105151, 4105169, 4105181, 4105183,
    };
    private Logger log = Logger.getLogger(getClass());
    private RecordDefinition recordDefinition;
    private BitSet[] bitSet;
    private boolean checkUniqueness;
    private String context;
    private int contextBegin, contextEnd;

    public RecordValidator(MetadataModel metadataModel, boolean checkUniqueness) {
        this.recordDefinition = metadataModel.getRecordDefinition();
        if (this.checkUniqueness = checkUniqueness) {
            bitSet = new BitSet[PRIMES.length];
            for (int walk = 0; walk < bitSet.length; walk++) {
                bitSet[walk] = new BitSet(PRIMES[walk]);
            }
        }
        StringBuilder contextString = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n\n<validate\n");
        for (NamespaceDefinition namespaceDefinition : recordDefinition.namespaces) {
            contextString.append(String.format("xmlns:%s=\"%s\"\n", namespaceDefinition.prefix, namespaceDefinition.uri));
        }
        contextString.append(">\n%s</validate>\n");
        this.context = contextString.toString();
        this.contextBegin = this.context.indexOf("%s");
        this.contextEnd = this.context.length() - (this.contextBegin + 2);
    }

    public String validate(String recordString, List<String> problems) {
        String contextualizedRecord = String.format(context, recordString);
        StringWriter out = new StringWriter();
        try {
            Document document = DocumentHelper.parseText(contextualizedRecord);
            validate(document, problems, new TreeSet<String>(), new TreeMap<Path,Counter>());
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
        }
        catch (Exception e) {
            problems.add("Problem parsing: " + e.toString());
            return "Invalid";
        }
        out.getBuffer().delete(0, contextBegin);
        out.getBuffer().delete(out.getBuffer().length() - contextEnd, out.getBuffer().length());
        return out.toString();
    }

    private void validate(Document document, List<String> problems, Set<String> entries, Map<Path,Counter> counters) {
        Element validateElement = document.getRootElement();
        Element recordElement = validateElement.element("record");
        if (recordElement == null) {
            problems.add("Problem: Missing record element");
            return;
        }
        validate(recordElement, new Path(), problems, entries, counters);
    }

    private boolean validate(Element element, Path path, List<String> problems, Set<String> entries, Map<Path,Counter> counters) {
        path.push(Tag.create(element.getNamespacePrefix(), element.getName()));
        boolean hasElements = false;
        Iterator walk = element.elementIterator();
        while (walk.hasNext()) {
            Element subelement = (Element) walk.next();
            boolean remove = validate(subelement, path, problems, entries, counters);
            if (remove) {
                walk.remove();
            }
            hasElements = true;
        }
        if (!hasElements) {
            boolean fieldRemove = validate(element.getTextTrim(), path, problems, entries, counters);
            path.pop();
            return fieldRemove;
        }
        path.pop();
        return false;
    }

    private boolean validate(String text, Path path, List<String> problems, Set<String> entries, Map<Path,Counter> counters) {
        FieldDefinition field = recordDefinition.getFieldDefinition(path);
        if (field == null) {
            problems.add(String.format("No field definition found for path [%s]", path));
            return true;
        }
        String entryString = field + "=" + text;
        if (text.isEmpty() || entries.contains(entryString)) {
            return true;
        }
        else {
            log.info("Field: " + field);
            log.info(String.format("Validate [%s] content [%s]", field.path, text));
            entries.add(entryString);
            Counter counter = counters.get(field.path);
            if (counter == null) {
                counters.put(field.path, counter = new Counter());
            }
            counter.count++;
            if (!field.multivalued && counter.count > 1) {
                problems.add(String.format("Single-valued field [%s] has more than one value", field.path));
            }
            validate(text, field, problems);
            return false;
        }
    }

    private void validate(String text, FieldDefinition field, List<String> problems) {
        if (field.options != null && !field.valueMapped && !field.options.contains(text)) {
            String optionsString = getOptionsString(field);
            problems.add(String.format("Value for [%s] was [%s] which does not belong to [%s]", field.path, text, optionsString));
        }
        if (field.url) {
            try {
                new URL(text);
            }
            catch (MalformedURLException e) {
                problems.add(String.format("URL value for [%s] was [%s] which is malformed", field.path, text));
            }
        }
        String regex = field.regularExpression;
        if (regex != null) {
            if (!text.matches(regex)) {
                problems.add(String.format("Value for [%s] was [%s] which does not match regular expression [%s]", field.path, text, regex));
            }
        }
        if (field.id && checkUniqueness) {
            checkEntryUniqueness(text, field, problems);
        }
    }

    /*
    private void validateAgainstAnnotations(List<FieldEntry> fieldEntries, List<String> problems) {
        Map<Path, Counter> counterMap = new HashMap<Path, Counter>();
        for (FieldEntry fieldEntry : fieldEntries) {
            FieldDefinition field = fieldMap.get(fieldEntry.getTag());
            if (field == null) {
                problems.add(String.format("Unknown XML element [%s]", fieldEntry.getTag()));
            }
            else {
                String regex = field.regularExpression;
                if (regex != null) {
                    if (!fieldEntry.getValue().matches(regex)) {
                        problems.add(String.format("Value for [%s] was [%s] which does not match regular expression [%s]", fieldEntry.getTag(), fieldEntry.getValue(), regex));
                    }
                }
                if (field.id && checkUniqueness) {
                    checkEntryUniqueness(problems, fieldEntry);
                }
            }
        }
        Map<String, Boolean> present = new TreeMap<String, Boolean>();
        for (FieldDefinition field : fieldMap.values()) {
            if (field.requiredGroup != null) {
                present.put(field.requiredGroup, false);
            }
        }
        for (FieldEntry fieldEntry : fieldEntries) {
            FieldDefinition field = fieldMap.get(fieldEntry.getTag());
            if (field != null && field.requiredGroup != null) {
                present.put(field.requiredGroup, true);
            }
        }
        for (Map.Entry<String, Boolean> entry : present.entrySet()) {
            if (!entry.getValue()) {
                problems.add(String.format("Required field violation for [%s]", entry.getKey()));
            }
        }
        for (FieldDefinition field : fieldMap.values()) {
            Counter counter = counterMap.get(field.path);
            if (counter != null && !field.multivalued && counter.count > 1) {
                problems.add(String.format("Single-valued field [%s] had %d values", field.getTag(), counter.count));
            }
        }
    }


    private static class Counter {
        int count;
    }
    */

    private void checkEntryUniqueness(String text, FieldDefinition field, List<String> problems) {
        int hashCode = text.hashCode();
        boolean setEverywhere = true;
        for (int walk = 0; walk < bitSet.length && setEverywhere; walk++) {
            if (!getBit(hashCode, walk)) {
                setEverywhere = false;
            }
        }
        if (setEverywhere) {
            problems.add(String.format("Identifier [%s] must be unique but the value [%s] appears more than once", field.path, text));
        }
        else {
            for (int walk = 0; walk < bitSet.length; walk++) {
                setBit(hashCode, walk);
            }
        }
    }

    private boolean getBit(int hashCode, int bitSetIndex) {
        int offset = PRIMES[(bitSetIndex + 1) % PRIMES.length];
        int bitNumber = Math.abs((hashCode + offset) % bitSet[bitSetIndex].size());
        return bitSet[bitSetIndex].get(bitNumber);
    }

    private void setBit(int hashCode, int bitSetIndex) {
        int offset = PRIMES[(bitSetIndex + 1) % PRIMES.length];
        int bitNumber = Math.abs((hashCode + offset) % bitSet[bitSetIndex].size());
        bitSet[bitSetIndex].set(bitNumber);
    }

    private String getOptionsString(FieldDefinition field) {
        StringBuilder enumString = new StringBuilder();
        Iterator<String> walk = field.options.iterator();
        while (walk.hasNext()) {
            enumString.append(walk.next());
            if (walk.hasNext()) {
                enumString.append(',');
            }
        }
        return enumString.toString();
    }

    private static class Counter {
        int count;
    }

}