/*
 * Copyright 2010 DELVING BV
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

package eu.delving.metadata;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Parse, filter, validate a record
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
    private Uniqueness idUniqueness;
    private String context;
    private int contextBegin, contextEnd;

    public RecordValidator(MetadataModel metadataModel, boolean checkUniqueness) {
        this.recordDefinition = metadataModel.getRecordDefinition();
        this.idUniqueness = checkUniqueness ? new Uniqueness() : null;
        StringBuilder contextString = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<validate\n");
        for (NamespaceDefinition namespaceDefinition : recordDefinition.namespaces) {
            contextString.append(String.format("xmlns:%s=\"%s\"\n", namespaceDefinition.prefix, namespaceDefinition.uri));
        }
        contextString.append(">\n%s</validate>\n");
        this.context = contextString.toString();
        this.contextBegin = this.context.indexOf("%s");
        int afterPercentS = contextBegin + 2;
        this.contextEnd = this.context.length() - afterPercentS;
    }

    public String validateRecord(String recordString, List<String> problems) {
        if (!recordString.contains("<")) {
            return recordString;
        }
        String contextualizedRecord = String.format(context, recordString);
        StringWriter out = new StringWriter();
        try {
            Document document = DocumentHelper.parseText(contextualizedRecord);
            Map<Path, Counter> counters = new TreeMap<Path, Counter>();
            validateDocument(document, problems, new TreeSet<String>(), counters);
            validateCardinalities(counters, problems);
            XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
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

    private void validateCardinalities(Map<Path, Counter> counters, List<String> problems) {
        Map<String, Boolean> requiredGroupMap = new TreeMap<String, Boolean>();
        for (FieldDefinition field : recordDefinition.getMappableFields()) {
            if (field.requiredGroup != null) {
                requiredGroupMap.put(field.requiredGroup, false);
            }
            Counter counter = counters.get(field.path);
            if (!field.multivalued && counter != null && counter.count > 1) {
                problems.add(String.format("Single-valued field [%s] has more than one value", field.path));
            }
        }
        for (Map.Entry<Path, Counter> entry : counters.entrySet()) {
            FieldDefinition field = recordDefinition.getFieldDefinition(entry.getKey());
            if (field.requiredGroup != null) {
                requiredGroupMap.put(field.requiredGroup, true);
            }
        }
        for (Map.Entry<String, Boolean> entry : requiredGroupMap.entrySet()) {
            if (!entry.getValue()) {
                problems.add(String.format("Required field violation for [%s]", entry.getKey()));
            }
        }
    }

    private void validateDocument(Document document, List<String> problems, Set<String> entries, Map<Path, Counter> counters) {
        Element validateElement = document.getRootElement();
        Element recordElement = validateElement.element("record");
        if (recordElement == null) {
            problems.add("Problem: Missing record element");
            return;
        }
        validateElement(recordElement, new Path(), problems, entries, counters);
    }

    private boolean validateElement(Element element, Path path, List<String> problems, Set<String> entries, Map<Path, Counter> counters) {
        path.push(Tag.create(element.getNamespacePrefix(), element.getName()));
        boolean hasElements = false;
        Iterator walk = element.elementIterator();
        while (walk.hasNext()) {
            Element subelement = (Element) walk.next();
            boolean remove = validateElement(subelement, path, problems, entries, counters);
            if (remove) {
                walk.remove();
            }
            hasElements = true;
        }
        if (!hasElements) {
            boolean fieldRemove = validatePath(element.getTextTrim(), path, problems, entries, counters);
            path.pop();
            return fieldRemove;
        }
        path.pop();
        return false;
    }

    private boolean validatePath(String text, Path path, List<String> problems, Set<String> entries, Map<Path, Counter> counters) {
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
            entries.add(entryString);
            Counter counter = counters.get(field.path);
            if (counter == null) {
                counters.put(field.path, counter = new Counter());
            }
            counter.count++;
            validateField(text, field, problems);
            return false;
        }
    }

    private void validateField(String text, FieldDefinition field, List<String> problems) {
        if (field.factDefinition != null && !field.valueMapped && field.factDefinition.options != null && !field.factDefinition.options.contains(text)) {
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
        if (field.id && idUniqueness != null) {
            if (idUniqueness.isRepeated(text)) {
                problems.add(String.format("Identifier [%s] must be unique but the value [%s] appears more than once", field.path, text));
            }
        }
    }

    private String getOptionsString(FieldDefinition field) {
        StringBuilder enumString = new StringBuilder();
        Iterator<String> walk = field.factDefinition.options.iterator();
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