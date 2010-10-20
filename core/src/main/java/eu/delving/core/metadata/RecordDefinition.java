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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("record-definition")
public class RecordDefinition {

    public ElementDefinition root;

    public Map<String, FieldDefinition> getConstantFields() {
        Map<String, FieldDefinition> map = new TreeMap<String, FieldDefinition>();
        root.getConstantFields("", map);
        return map;
    }

    public String toString() {
        return toString(this);
    }

    // handy static methods

    public static RecordDefinition read(InputStream in) {
        return (RecordDefinition) stream().fromXML(in);
    }

    public static String toString(RecordDefinition recordDefinition) {
        return stream().toXML(recordDefinition);
    }

    private static XStream stream() {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[]{
                RecordDefinition.class,
                ElementDefinition.class,
                FieldDefinition.class
        });
        return stream;
    }
}
