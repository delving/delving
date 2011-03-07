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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.MappingNotFoundException;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static eu.delving.core.util.MongoObject.mob;

/**
 * Implementing the record interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class RecordImpl implements MetaRepo.Record {
    private Logger log = Logger.getLogger(getClass());

    private DBCollection collection;
    private DBObject object;
    private String defaultPrefix;
    private DBObject namespaces;

    RecordImpl(DBCollection collection, DBObject object, String defaultPrefix, DBObject namespaces) {
        this.collection = collection;
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
        Boolean deleted = (Boolean) object.get(DELETED);
        return deleted != null && deleted;
    }

    @Override
    public DBObject getNamespaces() {
        return namespaces;
    }

    @Override
    public DBObject getHash() {
        return (DBObject) object.get(HASH);
    }

    @Override
    public Map<String, Integer> getFingerprint() {
        Map<String,Integer> fingerprint = new TreeMap<String,Integer>();
        DBObject hash = getHash();
        for (String key : hash.keySet()) {
            String path = (String) hash.get(key);
            int count = collection.find(
                    mob(
                            String.format("%s.%s", HASH, key),
                            mob("$exists", true)
                    )
            ).count();
            fingerprint.put(path, count);
        }
        return fingerprint;
    }

    @Override
    public String getXmlString() throws MappingNotFoundException {
        return (String) object.get(defaultPrefix);
    }

    @Override
    public String getXmlString(String metadataPrefix) throws MappingNotFoundException {
        String xml = (String) object.get(metadataPrefix);
        if (xml == null) {
            String errorMessage = String.format("No record with prefix [%s]", metadataPrefix);
            log.error(errorMessage);
            throw new MappingNotFoundException(errorMessage);
        }
        if (defaultPrefix.equals(metadataPrefix)) {
            xml = String.format("<record>\n%s</record>\n", xml);
        }
        return xml;
    }

    void addFormat(MetaRepo.MetadataFormat metadataFormat, String recordString) {
        object.put(metadataFormat.getPrefix(), recordString);
    }

    DBObject getObject() {
        return object;
    }
}

