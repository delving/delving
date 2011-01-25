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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.MappingNotFoundException;
import org.apache.log4j.Logger;
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
    private Logger log = Logger.getLogger(getClass());
    private ImplFactory implFactory;
    private DBObject object;
    private List<MetaRepo.Record> records = new ArrayList<MetaRepo.Record>();

    HarvestStepImpl(ImplFactory implFactory, DBObject object) {
        this.implFactory = implFactory;
        this.object = object;
    }

    @Override
    public ObjectId getId() {
        return (ObjectId) object.get(MetaRepo.MONGO_ID);
    }

    @Override
    public ObjectId getFirstId() {
        return (ObjectId) object.get(FIRST_ID);
    }

    @Override
    public Date getExpiration() {
        Date expiration = (Date) object.get(EXPIRATION);
        if (expiration == null) {
            expiration = new Date(System.currentTimeMillis() + 1000 * implFactory.getHarvestStepSecondsToLive());
        }
        return expiration;
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
                    ObjectId afterId = getAfterId();
                    loop:
                    while (true) {
                        int recordsToFetch = implFactory.getResponseListSize() - getRecordCount() + 1; // 1 extra
                        MetaRepo.DataSet.RecordFetch recordFetch  = dataSet.getRecords(
                                getPmhRequest().getMetadataPrefix(),
                                recordsToFetch,
                                getPmhRequest().getFrom(),
                                afterId,
                                getPmhRequest().getUntil(),
                                key
                        );
                        if (recordFetch == null) {
                            break;
                        }
                        afterId = recordFetch.getAfterId();
                        log.info(String.format("Fetched %d records", recordFetch.getRecords().size()));
                        for (MetaRepo.Record record : recordFetch.getRecords()) {
                            if (addRecord(record) == implFactory.getResponseListSize()) { // full => prepare next step
                                log.info("Full at "+getRecordCount());
                                DBObject nextStep = insertNextStep(
                                        getCursor() + implFactory.getResponseListSize(),
                                        record.getId()
                                );
                                setNextId((ObjectId) nextStep.get(MetaRepo.MONGO_ID));
                                break loop;
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

            private DBObject insertNextStep(int cursor, ObjectId afterId) {
                log.info(String.format("Next step cursor=%d, after id %s", cursor, afterId));
                DBObject nextStep = new BasicDBObject(MetaRepo.HarvestStep.PMH_REQUEST, object.get(MetaRepo.HarvestStep.PMH_REQUEST));
                nextStep.put(MetaRepo.HarvestStep.NAMESPACES, object.get(MetaRepo.HarvestStep.NAMESPACES));
                nextStep.put(MetaRepo.HarvestStep.LIST_SIZE, object.get(MetaRepo.HarvestStep.LIST_SIZE));
                nextStep.put(MetaRepo.HarvestStep.FIRST_ID, object.get(MetaRepo.HarvestStep.FIRST_ID));
                nextStep.put(MetaRepo.HarvestStep.CURSOR, cursor);
                nextStep.put(MetaRepo.HarvestStep.AFTER_ID, afterId);
                implFactory.harvestSteps().save(nextStep);
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
        return records.size();
    }

    @Override
    public List<MetaRepo.Record> getRecords() {
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
    public ObjectId getAfterId() {
        return (ObjectId) object.get(AFTER_ID);
    }

    @Override
    public ObjectId getNextId() {
        return (ObjectId) object.get(NEXT_ID);
    }

    @Override
    public String getErrorMessage() {
        return (String) object.get(ERROR_MESSAGE);
    }

    @Override
    public void save() {
        implFactory.harvestSteps().save(object);
    }

    @Override
    public void delete() {
        implFactory.harvestSteps().remove(object);
    }

    private int addRecord(MetaRepo.Record record) {
        records.add(record);
        return records.size();
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

