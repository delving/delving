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

import java.io.IOException;
import java.net.URL;

/**
 * Implementing the MetadataModel inteface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetadataModelImpl implements MetadataModel {

    private RecordDefinition recordDefinition;

    public void setRecordDefinitionResource(String path) throws IOException {
        URL url = getClass().getResource(path);
        this.recordDefinition = RecordDefinition.read(url.openStream());
    }

    public RecordDefinition getRecordDefinition() {
        return recordDefinition;
    }
}
