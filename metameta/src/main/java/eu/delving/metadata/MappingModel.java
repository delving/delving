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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class holds a record mapping model, handles loading and saving, and
 * makes it observable.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MappingModel {

    private RecordMapping recordMapping;

    public void setRecordMapping(RecordMapping recordMapping) {
        this.recordMapping = recordMapping;
        fireChangeEvent();
    }

    public RecordMapping getRecordMapping() {
        if (recordMapping == null) {
            recordMapping = new RecordMapping();
        }
        return recordMapping;
    }

    public void setConstant(String path, String value) {
        if (value == null) {
            getRecordMapping().constants.remove(path);
        }
        else {
            getRecordMapping().constants.put(path, value);
        }
        fireChangeEvent();
    }

    public void setMapping(String path, FieldMapping fieldMapping) {
        if (fieldMapping == null) {
            getRecordMapping().fieldMappings.remove(path);
        }
        else {
            getRecordMapping().fieldMappings.put(path, fieldMapping);
        }
        fireChangeEvent();
    }

    public void clearFieldMappings() {
        getRecordMapping().fieldMappings.clear();
        fireChangeEvent();
    }

    // observable

    public interface Listener {
        void mappingChanged(RecordMapping recordMapping);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    private void fireChangeEvent() {
        for (Listener listener : listeners) {
            listener.mappingChanged(getRecordMapping());
        }
    }

}
