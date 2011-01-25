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

package eu.delving.core.storage;

import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

/**
 * Handle the storage of the different tokens
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface TokenRepo extends PersistentTokenRepository {

    String REGISTRATION_COLLECTION = "registration";
    String AUTHENTICATION_COLLECTION = "authentication";

    RegistrationToken createRegistrationToken(String email);

    RegistrationToken getRegistrationToken(String id);

    public interface RegistrationToken {

        String getId();
        String getEmail();
        boolean isOlderThan(long time);
        void delete();

        String EMAIL = "email";
        String ID = "_id";
    }

    public interface AuthenticationToken {

        String getTokenValue();

        String getSeries();

        String getEmail();

        Date getDate();

        String TOKEN_VALUE = "token_value";
        String SERIES = "series";
        String EMAIL = "email";
        String DATE = "date";
    }
}
