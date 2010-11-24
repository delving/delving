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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.List;

/**
 * An XStream approach for replacing the annotated beans.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("field")
public class FieldDefinition implements Comparable<FieldDefinition> {

    @XStreamAsAttribute
    public String prefix;
    @XStreamAsAttribute
    public String localName;
    
    public String facetPrefix;
    public boolean briefDoc = false;
    public boolean fullDoc = true;
    public boolean hidden = true;
    public boolean id = false;
    public boolean object = false;
    public boolean type = false;
    public String requiredGroup;
    public boolean constant = false;
    public String category;
    public String converterPattern;
    public boolean converterMultipleOutput = false;
    public boolean url = false;
    public String regularExpression;
    public boolean valueMapped = false;
    public String fieldType;
    public boolean multivalued = true;
    public boolean stored = true;
    public boolean indexed = true;
    public boolean compressed = false;
    public boolean termVectors = true;
    public boolean termPositions = true;
    public boolean termOffsets = true;
    public boolean omitNorms = true;
    public String defaultValue = "";
    public List<String> options;
    public List<String> toCopyField;

    @XStreamOmitField
    public Path path;

    private Tag tag;

    public Tag getTag() {
        if (tag == null) {
            tag = Tag.create(prefix, localName);
        }
        return tag;
    }

    public void setPath(Path path) {
        path.push(getTag());
        this.path = new Path(path);
        path.pop();
    }

    public String getFieldNameString() {
        if (getPrefix() == null) {
            return tag.getLocalName();
        }
        else {
            return getPrefix() + '_' + tag.getLocalName();
        }
    }

    public String getPrefix() {
        if (facetPrefix != null) {
            return facetPrefix;
        }
        else {
            return tag.getPrefix();
        }
    }

    public String getLocalName() {
        return tag.getLocalName();
    }

    public String getFacetName() {
        return tag.getLocalName().toUpperCase();
    }

    @Override
    public String toString() {
        return String.format("FieldDefinition(%s)", path);
    }

    @Override
    public int compareTo(FieldDefinition fieldDefinition) {
        return path.compareTo(fieldDefinition.path);
    }
}
