package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class describes how a field is mapped.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("field-mapping")
public class FieldMapping {

    @XStreamAlias("value-map")
    public Map<String, String> valueMap;

    @XStreamAlias("groovy-code")
    public List<String> code;

    @XStreamOmitField
    public FieldDefinition fieldDefinition;

    @XStreamOmitField
    public List<String> variables;

    public FieldMapping(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    public FieldDefinition getFieldDefinition() {
        if (fieldDefinition == null) {
            throw new IllegalStateException("Expected that FieldMapping has fieldDefinition");
        }
        return fieldDefinition;
    }

    public void addCodeLine(String line) {
        if (code == null) {
            code = new ArrayList<String>();
        }
        code.add(line);
        variables = null;
    }

    public void createValueMap(Set<String> domainValues) {
        this.valueMap = new TreeMap<String,String>();
        for (String key : domainValues) {
            this.valueMap.put(key,"");
        }
    }

    public List<String> getVariables() {
        if (variables == null) {
            variables = new ArrayList<String>();
            for (String line : code) {
                Matcher matcher = VARIABLE_PATTERN.matcher(line);
                while (matcher.find()) {
                    String var = matcher.group(0);
                    for (String toRemove : TO_REMOVE) {
                        if (var.endsWith(toRemove)) {
                            var = var.substring(0, var.length() - toRemove.length());
                            break;
                        }
                    }
                    variables.add(var);
                }
            }
        }
        return variables;
    }

    public boolean codeLooksLike(String codeString) {
        Iterator<String> walk = code.iterator();
        for (String line : codeString.split("\n")) {
            line = line.trim();
            if (!line.isEmpty()) {
                if (!walk.hasNext()) {
                    return false;
                }
                String codeLine = walk.next();
                if (!codeLine.equals(line)) {
                    return false;
                }
            }
        }
        return !walk.hasNext();
    }
    
    public void setCode(String code) {
        if (this.code == null) {
            this.code = new ArrayList<String>();
        }
        this.code.clear();
        this.code.addAll(Arrays.asList(code.split("\n")));
        this.variables = null;
    }

    public String getDescription() {
        String fieldName = fieldDefinition == null ? "?" : fieldDefinition.getFieldNameString();
        if (getVariables().isEmpty()) {
            return fieldName;
        }
        else {
            return fieldName + " from " + getVariables();
        }
    }

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("input(\\.\\w+)+");
    private static final String [] TO_REMOVE = {
            ".each",
            ".split"
    };

}
