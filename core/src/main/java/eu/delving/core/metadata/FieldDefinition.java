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

    public String getLocalName() {
        return localName;
    }

    @XStreamAsAttribute
    public String prefix;
    
    @XStreamAsAttribute
    public String localName;

    public String facetPrefix;
    public Boolean briefDoc;
    public Boolean fullDoc;
    public Boolean hidden;
    public Boolean id;
    public Boolean object;
    public Boolean type;
    public String requiredGroup;
    public Boolean constant;
    public String category;
    public String converter;
    public Boolean converterMultipleOutput;
    public Boolean url;
    public String regularExpression;
    public Boolean valueMapped;
    public String fieldType;
    public Boolean multivalued;
    public Boolean stored;
    public Boolean indexed;
    public Boolean required;
    public Boolean compressed;
    public Boolean termVectors;
    public Boolean termPositions;
    public Boolean termOffsets;
    public Boolean omitNorms;
    public String defaultValue;
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

    @Override
    public String toString() {
        return String.format("FieldDefinition(%s:%s)", prefix, localName);
    }

}
