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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import eu.delving.metadata.MetadataModel;
import eu.delving.services.core.MetaRepo;
import eu.delving.sip.AccessKey;
import eu.europeana.sip.core.GroovyCodeResource;

/**
 * Allow for foreign instantiations
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ImplFactory {
    private MetaRepo metaRepo;
    private DB db;
    private MetadataModel metadataModel;
    private GroovyCodeResource groovyCodeResource;
    private AccessKey accessKey;

    public ImplFactory(MetaRepo metaRepo, DB db, MetadataModel metadataModel, GroovyCodeResource groovyCodeResource, AccessKey accessKey) {
        this.metaRepo = metaRepo;
        this.db = db;
        this.metadataModel = metadataModel;
        this.groovyCodeResource = groovyCodeResource;
        this.accessKey = accessKey;
    }

    public DBCollection records(String spec) {
        return db.getCollection(MetaRepo.RECORD_COLLECTION_PREFIX + spec);
    }

    public DBCollection dataSets() {
        return db.getCollection(MetaRepo.DATASETS_COLLECTION);
    }

    public MetadataModel getMetadataModel() {
        return metadataModel;
    }

    public GroovyCodeResource getGroovyCodeResource() {
        return groovyCodeResource;
    }

    public AccessKey getAccessKey() {
        return accessKey;
    }

    public MetaRepo.DataSet createDataSet(DBObject object) {
        return new DataSetImpl(this, object);
    }

    public MetaRepo.HarvestStep createHarvestStep(DBObject object) {
        return new HarvestStepImpl(metaRepo, object);
    }

    public MetaRepo.Mapping createMapping(MetaRepo.DataSet dataSet, DBObject object) {
        return new MappingImpl(this, dataSet, object);
    }
}

