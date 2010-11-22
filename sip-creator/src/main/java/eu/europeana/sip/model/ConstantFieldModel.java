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

package eu.europeana.sip.model;

import eu.delving.core.metadata.ConstantInputDefinition;
import eu.delving.core.metadata.RecordDefinition;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.SourceDetails;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Brings together constants for source details as well as those in the mapping
 * into one for presentation in the constant field panel.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ConstantFieldModel {
    private List<ConstantInputDefinition> definitions = new ArrayList<ConstantInputDefinition>();
    private List<ConstantInputDefinition> recordDefinitionConstants;
    private Map<String, String> constantMap = new LinkedHashMap<String, String>();

    public List<ConstantInputDefinition> getDefinitions() {
        return definitions;
    }

    public void setRecordDefinition(RecordDefinition recordDefinition) {
        this.recordDefinitionConstants = recordDefinition.constants;
        definitions.clear();
        definitions.addAll(SourceDetails.definition().constants);
        loop:
        for (ConstantInputDefinition cid : recordDefinition.constants) {
            for (int walk = 0; walk < definitions.size(); walk++) { // first see if we can replace an existing one
                ConstantInputDefinition existing = definitions.get(walk);
                if (existing.name.equals(cid.name)) {
                    definitions.set(walk, cid);
                    continue loop;
                }
            }
            definitions.add(cid);
        }
        for (Listener listener : listeners) {
            listener.updatedDefinitions(this);
        }
    }

    public void setSourceDetails(SourceDetails sourceDetails) {
        boolean changed = false;
        for (ConstantInputDefinition cid : SourceDetails.definition().constants) {
            String oldValue = get(cid);
            String newValue = sourceDetails.get(cid.name);
            if (!oldValue.equals(newValue)) {
                if (put(cid, newValue)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            for (Listener listener : listeners) {
                listener.updatedConstant(this, true);
            }
        }
    }

    public boolean fillSourceDetails(SourceDetails sourceDetails) {
        boolean changed = false;
        for (ConstantInputDefinition cid : SourceDetails.definition().constants) {
            if (sourceDetails.set(cid.name, get(cid))) {
                changed = true;
            }
        }
        return changed;
    }

    public void setRecordMapping(RecordMapping recordMapping) {
        boolean changed = false;
        for (ConstantInputDefinition cid : recordDefinitionConstants) {
            String oldValue = get(cid);
            String newValue = recordMapping.getConstant(cid.name);
            if (!oldValue.equals(newValue)) {
                if (put(cid, newValue)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            for (Listener listener : listeners) {
                listener.updatedConstant(this, false);
            }
        }
    }

    public boolean fillRecordMapping(RecordMapping recordMapping) {
        boolean changed = false;
        for (ConstantInputDefinition cid : recordDefinitionConstants) {
            if (recordMapping.setConstant(cid.name, get(cid))) {
                changed = true;
            }
        }
        return changed;
    }

    public void set(ConstantInputDefinition cid, String value) {
        if (put(cid, value)) {
            for (Listener listener : listeners) {
                listener.updatedConstant(this, true);
            }
        }
    }

    public String get(ConstantInputDefinition cid) {
        String value = constantMap.get(cid.name);
        if (value == null) {
            constantMap.put(cid.name, value = "");
        }
        return value;
    }

    private boolean put(ConstantInputDefinition cid, String value) {
        String existing = get(cid);
        if (value.equals(existing)) {
            return false;
        }
        constantMap.put(cid.name, value);
        return true;
    }

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public interface Listener {
        void updatedDefinitions(ConstantFieldModel constantFieldModel);

        void updatedConstant(ConstantFieldModel constantFieldModel, boolean interactive);
    }

}
