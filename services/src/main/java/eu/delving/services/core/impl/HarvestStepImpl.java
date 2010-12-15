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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.MappingNotFoundException;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementing the record interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class HarvestStepImpl implements MetaRepo.HarvestStep {
    private ImplFactory implFactory;
    private DBObject object;

    HarvestStepImpl(ImplFactory implFactory, DBObject object) {
        this.implFactory = implFactory;
        this.object = object;
    }

    @Override
    public ObjectId getId() {
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
    public Runnable createRecordFetcher(final MetaRepo.DataSet dataSet, final String key) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    addRecord(null); // marking that we've tried
                    while (true) {
                        int recordsToFetch = implFactory.getResponseListSize() - getRecordCount() + 1; // 1 extra
                        List<? extends MetaRepo.Record> records = dataSet.getRecords(
                                getPmhRequest().getMetadataPrefix(),
                                recordsToFetch,
                                getAfter(),
                                getPmhRequest().getUntil(),
                                key
                        );
                        if (records == null) {
                            break;
                        }
                        for (MetaRepo.Record record : records) {
                            if (addRecord(record) == implFactory.getResponseListSize()) { // full => prepare next step
                                DBObject nextStep = insertNextStep(
                                        getCursor() + implFactory.getResponseListSize(),
                                        record.getModifiedDate() // being set by MongoObjectParser
                                );
                                setNextId((ObjectId) nextStep.get(MetaRepo.MONGO_ID));
                                implFactory.harvestSteps().save(object);
                                break;
                            }
                        }
                    }
                }
                catch (AccessKeyException e) {
                    object.put(ERROR_MESSAGE, e.toString());
                }
                catch (MappingNotFoundException e) {
                    object.put(ERROR_MESSAGE, e.toString());
                }
            }

            private DBObject insertNextStep(int cursor, Date after) {
                DBObject nextStep = new BasicDBObject(MetaRepo.HarvestStep.PMH_REQUEST, object.get(MetaRepo.HarvestStep.PMH_REQUEST));
                nextStep.put(MetaRepo.HarvestStep.NAMESPACES, object.get(MetaRepo.HarvestStep.NAMESPACES));
                nextStep.put(MetaRepo.HarvestStep.LIST_SIZE, object.get(MetaRepo.HarvestStep.LIST_SIZE));
                nextStep.put(MetaRepo.HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * implFactory.getHarvestStepSecondsToLive()));
                nextStep.put(MetaRepo.HarvestStep.CURSOR, cursor);
                nextStep.put(MetaRepo.HarvestStep.AFTER, after);
                implFactory.harvestSteps().insert(nextStep);
                return nextStep;
            }
        };
    }

    @Override
    public int getCursor() {
        return (Integer) object.get(CURSOR);
    }

    @Override
    public int getRecordCount() {
        BasicDBList recordList = (BasicDBList) object.get(RECORDS);
        return recordList == null ? 0 : recordList.size();
    }

    @Override
    public List<MetaRepo.Record> getRecords() {
        BasicDBList recordList = (BasicDBList) object.get(RECORDS);
        if (recordList == null) {
            return null;
        }
        List<MetaRepo.Record> records = new ArrayList<MetaRepo.Record>(recordList.size());
        for (Object element : recordList) {
            DBObject recordObject = (DBObject) element;
            records.add(new RecordImpl(recordObject, getPmhRequest().getMetadataPrefix(), getNamespaces()));
        }
        return records;
    }

    @Override
    public MetaRepo.PmhRequest getPmhRequest() {
        return new PmhRequestImpl((DBObject) object.get(PMH_REQUEST));
    }

    @Override
    public boolean hasNext() {
        return getNextId() != null;
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
        return getNextId().toString();
    }

    @Override
    public Date getAfter() {
        return (Date) object.get(AFTER);
    }

    @Override
    public ObjectId getNextId() {
        return (ObjectId) object.get(NEXT_ID);
    }

    @Override
    public String getErrorMessage() {
        return (String) object.get(ERROR_MESSAGE);
    }

    private int addRecord(MetaRepo.Record record) {
        BasicDBList recordList = (BasicDBList) object.get(RECORDS);
        if (recordList == null) {
            object.put(RECORDS, recordList = new BasicDBList());
        }
        if (record != null) {
            recordList.add(((RecordImpl) record).getObject());
        }
        return recordList.size();
    }

    public void setNextId(ObjectId objectId) {
        object.put(NEXT_ID, objectId);
    }

    public DBObject getObject() {
        return object;
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

