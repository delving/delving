/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.delving.services.core.impl;

import com.mongodb.DBObject;
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
    public byte[] getFacts() {
        return (byte[]) object.get(FACT_BYTES);
    }

    @Override
    public void setFacts(byte[] factBytes) {
        object.put(FACT_BYTES, factBytes);
    }

    @Override
    public int getTotalRecordCount() {
        final Object count = object.get(TOTAL_RECORDS);
        int totalCount = 0;
        if (count != null) {
            totalCount = Integer.parseInt(String.valueOf(count));
        }
        return totalCount;
    }


    @Override
    public void setTotalRecordCount(int count) {
        object.put(TOTAL_RECORDS, count);
    }


    @Override
    public int getDeletedRecordCount() {
        final Object count = object.get(DELETED_RECORDS);
        int deletedCount = 0;
        if (count != null) {
            deletedCount = Integer.parseInt(String.valueOf(count));
        }
        return deletedCount;
    }

    @Override
    public void setDeletedRecordCount(int count) {
        object.put(DELETED_RECORDS, count);
    }


    @Override
    public int getUploadedRecordCount() {
        final Object count = object.get(UPLOADED_RECORDS);
        int uploadedCount = 0;
        if (count != null) {
            uploadedCount = Integer.parseInt(String.valueOf(count));
        }
        return uploadedCount;
    }

    @Override
    public void setUploadedRecordCount(int count) {
        object.put(UPLOADED_RECORDS, count);
    }

    @Override
    public String getName() {
        return (String) object.get(NAME);
    }

    @Override
    public void setName(String value) {
        object.put(NAME, value);
    }

}

