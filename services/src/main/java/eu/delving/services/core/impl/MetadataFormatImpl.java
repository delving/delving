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
import eu.delving.services.core.MetaRepo;

/**
 * Implementing the metadata format interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class MetadataFormatImpl implements MetaRepo.MetadataFormat, Comparable<MetaRepo.MetadataFormat> {
    private DBObject object;

    MetadataFormatImpl(DBObject object) {
        this.object = object;
    }

    @Override
    public String getPrefix() {
        return (String) object.get(PREFIX);
    }

    @Override
    public void setPrefix(String value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty prefix");
        }
        object.put(PREFIX, value);
    }

    @Override
    public String getSchema() {
        return (String) object.get(SCHEMA);
    }

    @Override
    public void setSchema(String value) {
        object.put(SCHEMA, value);
    }

    @Override
    public String getNamespace() {
        return (String) object.get(NAMESPACE);
    }

    @Override
    public void setNamespace(String value) {
        object.put(NAMESPACE, value);
    }

    @Override
    public boolean isAccessKeyRequired() {
        Boolean isIt = (Boolean) object.get(ACCESS_KEY_REQUIRED);
        return isIt == null ? Boolean.FALSE : isIt;
    }

    @Override
    public void setAccessKeyRequired(boolean required) {
        object.put(ACCESS_KEY_REQUIRED, required);
    }

    @Override
    public int compareTo(MetaRepo.MetadataFormat o) {
        return getPrefix().compareTo(o.getPrefix());
    }
}

