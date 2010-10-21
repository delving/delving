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

package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

/**
 * An XStream approach for replacing the annotated beans.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("field")
public class FieldDefinition {
    
    public String getTag() {
        if (getPrefix() == null) {
            return localName;
        }
        else {
            return getPrefix() + ':' + localName;
        }
    }

    public String getFieldNameString() {
        if (getPrefix() == null) {
            return localName;
        }
        else {
            return getPrefix() + '_' + localName;
        }
    }

    public String getPrefix() {
        if (facetPrefix != null) {
            return facetPrefix;
        }
        else {
            return prefix;
        }
    }

    public String getFacetName() {
        return localName.toUpperCase();
    }

    public String getLocalName() {
        return localName;
    }

    @XStreamAsAttribute
    public String prefix;
    
    @XStreamAsAttribute
    public String localName;

    public String facetPrefix;
    public Boolean briefDoc = false;
    public Boolean fullDoc = true;
    public Boolean hidden = true;
    public Boolean id = false;
    public Boolean object = false;
    public Boolean type = false;
    public String requiredGroup;
    public Boolean constant = false;
    public String category;
    public String converter;
    public Boolean converterMultipleOutput = false;
    public Boolean url = false;
    public String regularExpression;
    public Boolean valueMapped = false;
    public String fieldType;
    public Boolean multivalued = true;
    public Boolean stored = true;
    public Boolean indexed = true;
    public Boolean required = false;
    public Boolean compressed = false;
    public Boolean termVectors = true;
    public Boolean termPositions = true;
    public Boolean termOffsets = true;
    public Boolean omitNorms = true;
    public String defaultValue = "";
    public List<String> options;
    public List<String> toCopyField;

    private Object readResolve() {
        multivalued = setDefaultTrue(multivalued);
        stored = setDefaultTrue(stored);
        indexed = setDefaultTrue(indexed);
        termVectors = setDefaultTrue(termVectors);
        return this;
    }

    private Boolean setDefaultTrue(Boolean value) {
        return value == null ? true : value;
    }

    public Boolean isId() {
        return id == null ? false : id;
    }

    public Boolean isType() {
        return type == null ? false : type;
    }

    @Override
    public String toString() {
        return String.format("FieldDefinition(%s:%s)", prefix, localName);
    }
}
