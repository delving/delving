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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A groovy mapping based on a model.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("record-mapping")
public class RecordMapping {

    @XStreamAsAttribute
    String prefix;

    @XStreamAlias("records-normalized")
    int recordsNormalized;

    @XStreamAlias("records-discarded")
    int recordsDiscarded;

    @XStreamAlias("normalize-time")
    long normalizeTime;

    @XStreamAlias("facts")
    Map<String, String> facts = new HashMap<String, String>();

    @XStreamAlias("field-mappings")
    Map<String, FieldMapping> fieldMappings = new HashMap<String, FieldMapping>();

    public RecordMapping(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getRecordsNormalized() {
        return recordsNormalized;
    }

    public void setRecordsNormalized(int recordsNormalized) {
        this.recordsNormalized = recordsNormalized;
    }

    public int getRecordsDiscarded() {
        return recordsDiscarded;
    }

    public void setRecordsDiscarded(int recordsDiscarded) {
        this.recordsDiscarded = recordsDiscarded;
    }

    public long getNormalizeTime() {
        return normalizeTime;
    }

    public void setNormalizeTime(long normalizeTime) {
        this.normalizeTime = normalizeTime;
    }

    public String getFact(String fieldName) {
        String value = facts.get(fieldName);
        if (value == null) {
            facts.put(fieldName, value = "");
        }
        return value;
    }

    public boolean setFact(String fieldName, String value) {
        String existing = facts.get(fieldName);
        if (existing == null || !value.equals(existing)) {
            facts.put(fieldName, value);
            return true;
        }
        else {
            return false;
        }
    }

    public Set<FieldMapping> getFieldMappings() {
        return new TreeSet<FieldMapping>(fieldMappings.values());
    }

    public void apply(RecordDefinition recordDefinition) throws MetadataException {
        for (Map.Entry<String, FieldMapping> entry : fieldMappings.entrySet()) {
            Path path = new Path(entry.getKey());
            FieldDefinition fieldDefinition = recordDefinition.getFieldDefinition(path);
            if (fieldDefinition == null) {
                throw new MetadataException("Field definition not found for " + path);
            }
            entry.getValue().fieldDefinition = fieldDefinition;
        }
    }

    public void apply(List<Statistics> statisticsList) {
        for (Map.Entry<String, FieldMapping> entry : fieldMappings.entrySet()) {
            Path path = new Path(entry.getKey());
        }
    }

    public FieldMapping getFieldMapping(String path) {
        return fieldMappings.get(path);
    }

    public String toDisplayCode(RecordDefinition recordDefinition) {
        return toCode(recordDefinition, null, false, null);
    }

    public String toCompileCode(RecordDefinition recordDefinition) {
        return toCode(recordDefinition, null, true, null);
    }

    public String toDisplayCode(RecordDefinition recordDefinition, String selectedPath) {
        return toCode(recordDefinition, selectedPath, false, null);
    }

    public String toCompileCode(RecordDefinition recordDefinition, String selectedPath) {
        return toCode(recordDefinition, selectedPath, true, null);
    }

    public String toCompileCode(RecordDefinition recordDefinition, String selectedPath, String editedCode) {
        return toCode(recordDefinition, selectedPath, true, editedCode);
    }

    public String toString() {
        return stream().toXML(this);
    }

    // === private

    private String toCode(RecordDefinition recordDefinition, String selectedPath, boolean forCompile, String editedCode) {
        final StringBuilder stringBuilder = new StringBuilder();
        Out out = new Out() {
            int indentLevel;

            @Override
            public void line(String line) {
                int spaces = indentLevel * 4;
                while (spaces-- > 0) {
                    stringBuilder.append(' ');
                }
                stringBuilder.append(line).append('\n');
            }

            @Override
            public void indent(int change) {
                indentLevel += change;
            }
        };
        if (forCompile) {
            out.line("// Groovy Mapping Code - Generated by SIP-Creator\n");
            out.line("// Constants\n");
            for (Map.Entry<String, String> factEntry : facts.entrySet()) {
                out.line(String.format(
                        "def %s = '%s'\n",
                        factEntry.getKey(),
                        escapeApostrophe(factEntry.getValue())
                ));
            }
            out.line("\n// Dictionaries\n");
            for (Map.Entry<String, FieldMapping> fieldMappingEntry : fieldMappings.entrySet()) {
                if (fieldMappingEntry.getValue().dictionary != null) {
                    String path = mungePath(fieldMappingEntry.getKey());
                    out.line(String.format(
                            "def %sDictionary = [\n",
                            path
                    ));
                    out.indent(1);
                    for (Map.Entry<String, String> entry : fieldMappingEntry.getValue().dictionary.entrySet()) {
                        out.line(String.format(
                                "'%s':'%s',",
                                escapeApostrophe(entry.getKey()),
                                escapeApostrophe(entry.getValue())
                        ));
                    }
                    out.indent(-1);
                    out.line("]");
                    out.line(String.format(
                            "def %s = { def v = %sDictionary[it.toString()]; return v ? v : it }\n",
                            path,
                            path
                    ));
                }
            }
            out.line("// Builder to create the record\n");
        }
        if (editedCode == null) {
            if (forCompile) {
                out.line("output.");
                out.indent(1);
            }
            Set<String> usedPaths = new TreeSet<String>();
            toCode("", recordDefinition.root, out, usedPaths, selectedPath, forCompile);
            if (forCompile) {
                out.indent(-1);
            }
            if (selectedPath == null && usedPaths.size() != fieldMappings.size()) {
                Set<String> unusedPaths = new TreeSet<String>(fieldMappings.keySet());
                unusedPaths.removeAll(usedPaths);
                Logger.getLogger(getClass()).warn("unused paths: " + unusedPaths);
            }
        }
        else {
            if (forCompile) {
                out.line("output.");
                out.line(String.format("%s {", recordDefinition.root.getTag()));
            }
            for (String line : editedCode.split("\n")) {
                out.line(line);
            }
            if (forCompile) {
                out.line("}");
            }
        }
        return stringBuilder.toString();
    }

    private void toCode(String path, ElementDefinition element, Out out, Set<String> usedPaths, String selectedPath, boolean forCompile) {
        if (selectedPath != null && !selectedPath.startsWith(path)) {
            return;
        }
        if (forCompile) {
            out.line(String.format("%s {", element.getTag()));
            out.indent(1);
        }
        if (element.elements != null) {
            for (ElementDefinition subNode : element.elements) {
                toCode(path + "/" + element.getTag(), subNode, out, usedPaths, selectedPath, forCompile);
            }
        }
        if (element.fields != null) {
            for (FieldDefinition fieldDefinition : element.fields) {
                toCode(path + "/" + element.getTag(), fieldDefinition, out, usedPaths, selectedPath);
            }
        }
        if (forCompile) {
            out.indent(-1);
            out.line("}");
        }
    }

    private void toCode(String path, FieldDefinition field, Out out, Set<String> usedPaths, String selectedPath) {
        String fieldPath = path + "/" + field.getTag();
        if (selectedPath != null && !selectedPath.equals(fieldPath)) {
            return;
        }
        FieldMapping fieldMapping = fieldMappings.get(fieldPath);
        if (fieldMapping != null) {
            usedPaths.add(fieldPath);
            for (String line : fieldMapping.code) {
                if (codeIndent(line) < 0) {
                    out.indent(-1);
                }
                out.line(line);
                if (codeIndent(line) > 0) {
                    out.indent(1);
                }
            }
        }
    }

    private static int codeIndent(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            switch (c) {
                case '}':
                    indent--;
                    break;
                case '{':
                    indent++;
                    break;
            }
        }
        return indent;
    }

    interface Out {
        void line(String line);

        void indent(int change);
    }

    public static String mungePath(String path) {
        return path.replaceAll("/", "_").replaceAll(":", "_").replaceAll("-", "_");
    }

    public static String escapeApostrophe(String s) {
        return s.replaceAll("'", "\\\\'");
    }

    public static void write(RecordMapping mapping, OutputStream out) {
        stream().toXML(mapping, out);
    }

    public static String toXml(RecordMapping mapping) {
        return stream().toXML(mapping);
    }

    public static RecordMapping read(InputStream is, MetadataModel metadataModel) throws MetadataException {
        RecordMapping recordMapping = (RecordMapping) stream().fromXML(is);
        RecordDefinition recordDefinition = metadataModel.getRecordDefinition(recordMapping.prefix);
        recordMapping.apply(recordDefinition);
        return recordMapping;
    }

    public static RecordMapping read(String string, MetadataModel metadataModel) throws MetadataException {
        RecordMapping recordMapping = (RecordMapping) stream().fromXML(string);
        RecordDefinition recordDefinition = metadataModel.getRecordDefinition(recordMapping.prefix);
        recordMapping.apply(recordDefinition);
        return recordMapping;
    }

    static XStream stream() {
        XStream stream = new XStream();
        stream.processAnnotations(RecordMapping.class);
        return stream;
    }

}
