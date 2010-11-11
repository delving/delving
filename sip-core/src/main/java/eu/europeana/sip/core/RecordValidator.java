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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    private Map<String, FieldDefinition> fieldMap = new HashMap<String, FieldDefinition>();
    private BitSet[] bitSet;
    private boolean checkUniqueness;
    private String context;
    private int contextBegin, contextEnd;

    public RecordValidator(MetadataModel metadataModel, boolean checkUniqueness) {
        if (this.checkUniqueness = checkUniqueness) {
            bitSet = new BitSet[PRIMES.length];
            for (int walk = 0; walk < bitSet.length; walk++) {
                bitSet[walk] = new BitSet(PRIMES[walk]);
            }
        }
        this.fieldMap = metadataModel.getRecordDefinition().getMappableFields();
        StringBuilder contextString = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n\n<validate\n");
        for (NamespaceDefinition namespaceDefinition : metadataModel.getRecordDefinition().namespaces) {
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
            validate(document);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
        }
        catch (Exception e) {
            problems.add("Problem parsing: " + e.toString());
        }
        out.getBuffer().delete(0, contextBegin);
        out.getBuffer().delete(out.getBuffer().length() - contextEnd, out.getBuffer().length());
        return out.toString();
    }

    private void validate(Document document) {
        validate(document.getRootElement(), new Path());
    }

    private void validate(Element element, Path path) {
        path.push(Tag.create(element.getNamespacePrefix(), element.getName()));
        if (element.hasContent()) {
            log.info(String.format("Validate [%s] content [%s]", path.toString(), element.getTextTrim()));
        }
    }

    private void validateAgainstAnnotations(List<FieldEntry> fieldEntries, List<String> problems) {
        Map<Path, Counter> counterMap = new HashMap<Path, Counter>();
        for (FieldEntry fieldEntry : fieldEntries) {
            FieldDefinition field = fieldMap.get(fieldEntry.getTag());
            if (field == null) {
                problems.add(String.format("Unknown XML element [%s]", fieldEntry.getTag()));
            }
            else {
                Counter counter = counterMap.get(fieldEntry.getPath());
                if (counter == null) {
                    counter = new Counter();
                    counterMap.put(fieldEntry.getPath(), counter);
                }
                counter.count++;
                if (field.options != null && !field.valueMapped && !field.options.contains(fieldEntry.getValue())) {
                    StringBuilder enumString = new StringBuilder();
                    Iterator<String> walk = field.options.iterator();
                    while (walk.hasNext()) {
                        enumString.append(walk.next());
                        if (walk.hasNext()) {
                            enumString.append(',');
                        }
                    }
                    problems.add(String.format("Value for [%s] was [%s] which does not belong to [%s]", fieldEntry.getTag(), fieldEntry.getValue(), enumString.toString()));
                }
//                if (field.constant && constantMap != null) {
//                    String value = constantMap.get(fieldEntry.getTag());
//                    if (value == null) {
//                        constantMap.put(fieldEntry.getTag(), fieldEntry.getValue());
//                    }
//                    else if (!value.equals(fieldEntry.getValue())) {
//                        problems.add(String.format("Value for [%s] should be constant but it had multiple values [%s] and [%s]", fieldEntry.getTag(), fieldEntry.getValue(), value));
//                    }
//                }
                String regex = field.regularExpression;
                if (regex != null) {
                    if (!fieldEntry.getValue().matches(regex)) {
                        problems.add(String.format("Value for [%s] was [%s] which does not match regular expression [%s]", fieldEntry.getTag(), fieldEntry.getValue(), regex));
                    }
                }
                if (field.url) {
                    try {
                        new URL(fieldEntry.getValue());
                    }
                    catch (MalformedURLException e) {
                        problems.add(String.format("URL value for [%s] was [%s] which is malformed", fieldEntry.getTag(), fieldEntry.getValue()));
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

    private void checkEntryUniqueness(List<String> problems, FieldEntry fieldEntry) {
        int hashCode = fieldEntry.getValue().hashCode();
        boolean setEverywhere = true;
        for (int walk = 0; walk < bitSet.length && setEverywhere; walk++) {
            if (!getBit(hashCode, walk)) {
                setEverywhere = false;
            }
        }
        if (setEverywhere) {
            problems.add(String.format("Identifier [%s] must be unique but the value [%s] appears more than once", fieldEntry.getTag(), fieldEntry.getValue()));
        }
        for (int walk = 0; walk < bitSet.length && setEverywhere; walk++) {
            setBit(hashCode, walk);
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

    private static class Counter {
        int count;
    }
}