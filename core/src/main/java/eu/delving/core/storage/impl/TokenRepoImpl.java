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

package eu.delving.core.storage.impl;

import com.mongodb.*;
import eu.delving.core.storage.TokenRepo;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

/**
 * Implements TokeRepo
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TokenRepoImpl implements TokenRepo {
    private Logger log = Logger.getLogger(getClass());
    private DB mongoDatabase;

    @Value("#{launchProperties['portal.mongo.dbName ']}")
    private String databaseName;

    @Autowired
    private Mongo mongo;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public RegistrationToken createRegistrationToken(String email) {
        DBObject object = new BasicDBObject(TokenRepo.RegistrationToken.EMAIL, email);
        reg().save(object);
        return new RegTokenImpl(object);
    }

    @Override
    public RegistrationToken getRegistrationToken(String id) {
        DBObject found = reg().findOne(new BasicDBObject(TokenRepo.RegistrationToken.ID, new ObjectId(id)));
        return found == null ? null : new RegTokenImpl(found);
    }

    @Override
    public void createNewToken(PersistentRememberMeToken prmt) {
        AuthTokenImpl token = new AuthTokenImpl();
        token.setSeries(prmt.getSeries());
        token.setEmail(prmt.getUsername());
        token.setDate(prmt.getDate());
        token.setTokenValue(prmt.getTokenValue());
        token.save();
        log.debug("created authorization token to "+prmt.getTokenValue());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        DBObject object = auth().findOne(new BasicDBObject(AuthenticationToken.SERIES, series));
        if (object == null) {
            log.warn("unable to update token " + series);
        }
        else {
            AuthTokenImpl token = new AuthTokenImpl(object);
            token.setTokenValue(tokenValue);
            token.setDate(lastUsed);
            token.save();
            log.debug("updated authorization token to "+tokenValue);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        DBObject object = auth().findOne(new BasicDBObject(AuthenticationToken.SERIES, series));
        if (object == null) {
            log.warn("unable to get token for series " + series);
            return null;
        }
        else {
            log.info("fetched token for series " + series);
            AuthenticationToken token = new AuthTokenImpl(object);
            return new PersistentRememberMeToken(
                    token.getEmail(),
                    token.getSeries(),
                    token.getTokenValue(),
                    token.getDate()
            );
        }
    }

    @Override
    public void removeUserTokens(String email) {
        log.info("removing token  for" + email);
        auth().remove(new BasicDBObject(AuthenticationToken.EMAIL, email));
    }

    private class RegTokenImpl implements RegistrationToken {

        private DBObject object;

        private RegTokenImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public String getId() {
            return object.get(ID).toString();
        }

        @Override
        public String getEmail() {
            return (String) object.get(EMAIL);
        }

        @Override
        public boolean isOlderThan(long time) {
            ObjectId objectId = (ObjectId) object.get(ID);
            return objectId.getTime() < time;
        }

        @Override
        public void delete() {
            reg().remove(object);
        }
    }

    private class AuthTokenImpl implements AuthenticationToken {
        private DBObject object;

        private AuthTokenImpl() {
            object = new BasicDBObject();
        }

        private AuthTokenImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public String getTokenValue() {
            return (String) object.get(TOKEN_VALUE);
        }

        @Override
        public String getSeries() {
            return (String) object.get(SERIES);
        }

        @Override
        public String getEmail() {
            return (String) object.get(EMAIL);
        }

        @Override
        public Date getDate() {
            return (Date) object.get(DATE);
        }

        public void setTokenValue(String tokenValue) {
            object.put(TOKEN_VALUE, tokenValue);
        }

        public void setSeries(String series) {
            object.put(SERIES, series);
        }

        public void setEmail(String email) {
            object.put(EMAIL, email);
        }

        public void setDate(Date date) {
            object.put(DATE, date);
        }

        public String toString() {
            return String.format("Authentication(%s) : %s", getEmail(), getTokenValue());
        }

        public void save() {
            auth().save(object);
        }
    }

    private DBCollection reg() {
        return db().getCollection(REGISTRATION_COLLECTION);
    }

    private DBCollection auth() {
        DBCollection coll = db().getCollection(AUTHENTICATION_COLLECTION);
        coll.ensureIndex(new BasicDBObject(AuthenticationToken.SERIES, 1));
        coll.ensureIndex(new BasicDBObject(AuthenticationToken.EMAIL, 1));
        return coll;
    }

    private synchronized DB db() {
        if (mongoDatabase == null) {
            mongoDatabase = mongo.getDB(databaseName);
        }
        return mongoDatabase;
    }
}
