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
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.MappingNotFoundException;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Implementing the record interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class HarvestStepImpl implements MetaRepo.HarvestStep {
    private MetaRepo metaRepo;
    private DBObject object;
    private ObjectId nextStepId;

    HarvestStepImpl(MetaRepo metaRepo, DBObject object) {
        this.metaRepo = metaRepo;
        this.object = object;
    }

    @Override
    public ObjectId getResumptionToken() {
        return (ObjectId) object.get(MetaRepo.MONGO_ID);
    }

    @Override
    public Date getExpiration() {
        return (Date) object.get(EXPIRATION);
    }

    @Override
    public int getListSize() {
        return (Integer) object.get(LIST_SIZE);
    }

    @Override
    public int getCursor() {
        return (Integer) object.get(CURSOR);
    }

    @Override
    public List<? extends MetaRepo.Record> getRecords() throws DataSetNotFoundException, MappingNotFoundException, AccessKeyException {
        MetaRepo.PmhRequest request = getPmhRequest();
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(request.getSet());
        if (dataSet == null) {
            throw new DataSetNotFoundException("No data set found by the name " + request.getSet());
        }
        String accessKey = (String) object.get(ACCESS_KEY);
        return dataSet.getRecords(request.getMetadataPrefix(), getCursor(), getListSize(), request.getFrom(), request.getUntil(), accessKey);
    }

    @Override
    public MetaRepo.PmhRequest getPmhRequest() {
        return new PmhRequestImpl((DBObject) object.get(PMH_REQUEST));
    }

    @Override
    public boolean hasNext() {
        return nextStepId != null;
    }

    @Override
    public DBObject getNamespaces() {
        return (DBObject) object.get(NAMESPACES);
    }

    @Override
    public String nextResumptionToken() {
        if (!hasNext()) {
            throw new RuntimeException("Should have checked hasNext()");
        }
        return nextStepId.toString();
    }

    @Override
    public void setNextStepId(ObjectId objectId) {
        this.nextStepId = objectId;
    }

    private static class PmhRequestImpl implements MetaRepo.PmhRequest {

        private DBObject object;

        private PmhRequestImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public MetaRepo.PmhVerb getVerb() {
            return MetaRepo.PmhVerb.valueOf((String) object.get(VERB));
        }

        @Override
        public String getSet() {
            return (String) object.get(SET);
        }

        @Override
        public Date getFrom() {
            return (Date) object.get(FROM);
        }

        @Override
        public Date getUntil() {
            return (Date) object.get(UNTIL);
        }

        @Override
        public String getMetadataPrefix() {
            return (String) object.get(PREFIX);
        }

        @Override
        public String getIdentifier() {
            return (String) object.get(IDENTIFIER);
        }
    }

}

