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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import eu.delving.core.storage.TokenRepo;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

/**
 * Implements TokeRepo
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TokenRepoImpl implements TokenRepo {

    @Override
    public RegistrationToken createRegistrationToken(String email) {
        return null;  // todo: implement
    }

    @Override
    public RegistrationToken getRegistrationToken(String id) {
        return null;  // todo: implement
    }

    @Override
    public void createNewToken(PersistentRememberMeToken prmt) {
        AuthTokenImpl token = new AuthTokenImpl();
        token.setSeries(prmt.getSeries());
        token.setEmail(prmt.getUsername());
        token.setId(prmt.getTokenValue());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        // todo: implement
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        AuthenticationToken token = new AuthTokenImpl();// todo: fetch
        return new PersistentRememberMeToken(
                token.getEmail(),
                token.getSeries(),
                token.getId(),
                token.getLastUsed()
        );
    }

    @Override
    public void removeUserTokens(String email) {
        // todo: implement
    }

    private class RegTokenIml implements RegistrationToken {

        @Override
        public String getId() {
            return null;  // todo: implement
        }

        @Override
        public String getEmail() {
            return null;  // todo: implement
        }

        @Override
        public void isOlderThan(long time) {
            // todo: implement
        }

        @Override
        public void delete() {
            // todo: implement
        }
    }

    private class AuthTokenImpl implements AuthenticationToken {
        private DBObject object = new BasicDBObject();

        @Override
        public String getId() {
            return null;  // todo: implement
        }

        @Override
        public String getSeries() {
            return null;  // todo: implement
        }

        @Override
        public String getEmail() {
            return null;  // todo: implement
        }

        @Override
        public Date getLastUsed() {
            return null;  // todo: implement
        }

        public void setId(String id) {

        }

        public void setSeries(String series) {

        }

        public void setEmail(String email) {

        }

        public void markUsed() {

        }
    }
}
