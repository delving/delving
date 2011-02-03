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

package eu.delving.services.core.impl;

import com.mongodb.DBObject;
import eu.delving.metadata.Path;
import eu.delving.services.core.MetaRepo;

import static eu.delving.core.util.MongoObject.mob;

/**
 * Implementing the details interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class DetailsImpl implements MetaRepo.Details {
    private DBObject object;
    private MetaRepo.MetadataFormat metadataFormat;

    DetailsImpl(DBObject object) {
        this.object = object;
    }

    @Override
    public MetaRepo.MetadataFormat getMetadataFormat() {
        if (metadataFormat == null) {
            DBObject formatObject = (DBObject) object.get(METADATA_FORMAT);
            if (formatObject == null) {
                object.put(METADATA_FORMAT, formatObject = mob());
            }
            metadataFormat = new MetadataFormatImpl(formatObject);
        }
        return metadataFormat;
    }

    @Override
    public Path getRecordRoot() {
        return new Path((String) (object.get(RECORD_ROOT)));
    }

    @Override
    public void setRecordRoot(Path path) {
        object.put(RECORD_ROOT, path.toString());
    }

    @Override
    public Path getUniqueElement() {
        return new Path((String) (object.get(UNIQUE_ELEMENT)));
    }

    @Override
    public void setUniqueElement(Path path) {
        object.put(UNIQUE_ELEMENT, path.toString());
    }

    @Override
    public String getName() {
        return (String) object.get(NAME);
    }

    @Override
    public void setName(String value) {
        object.put(NAME, value);
    }

    @Override
    public String getProviderName() {
        return (String) object.get(PROVIDER_NAME);
    }

    @Override
    public void setProviderName(String value) {
        object.put(PROVIDER_NAME, value);
    }

    @Override
    public String getDescription() {
        return (String) object.get(DESCRIPTION);
    }

    @Override
    public void setDescription(String value) {
        object.put(DESCRIPTION, value);
    }

}

