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
import eu.delving.services.exceptions.MappingNotFoundException;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Implementing the record interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class RecordImpl implements MetaRepo.Record {
    private Logger log = Logger.getLogger(getClass());

    private DBObject object;
    private String defaultPrefix;
    private DBObject namespaces;

    RecordImpl(DBObject object, String defaultPrefix, DBObject namespaces) {
        this.object = object;
        this.defaultPrefix = defaultPrefix;
        this.namespaces = namespaces;
    }

    @Override
    public ObjectId getId() {
        return (ObjectId) object.get(MetaRepo.MONGO_ID);
    }

    @Override
    public String getUnique() {
        return (String) object.get(UNIQUE);
    }

    @Override
    public Date getModifiedDate() {
        return (Date) object.get(MODIFIED);
    }

    @Override
    public boolean isDeleted() {
        return false;  //TODO: implement this
    }

    @Override
    public DBObject getNamespaces() {
        return namespaces;
    }

    @Override
    public String getXmlString() throws MappingNotFoundException {
        return getXmlString(defaultPrefix);
    }

    // todo determine if the right format is returned after on-the-fly mapping

    @Override
    public String getXmlString(String metadataPrefix) throws MappingNotFoundException {
        String x = (String) object.get(metadataPrefix);
        if (x == null) {
            String errorMessage = String.format("No record with prefix [%s]", metadataPrefix);
            log.error(errorMessage);
            throw new MappingNotFoundException(errorMessage);
        }
        return x;
    }

    void addFormat(MetaRepo.MetadataFormat metadataFormat, String recordString) {
        object.put(metadataFormat.getPrefix(), recordString);
    }

    DBObject getObject() {
        return object;
    }
}

