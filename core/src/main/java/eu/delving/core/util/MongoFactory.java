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

package eu.delving.core.util;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 3/1/11 10:19 PM
 */

public class MongoFactory implements InitializingBean {

    private Mongo mongo;
    private boolean testContext;
    private List<ServerAddress> mongoAddresses;
    private MongoOptions mongoOptions = new MongoOptions();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (testContext) {
            this.mongo = new Mongo();
        } else {
            if (mongoAddresses.isEmpty() || mongoAddresses.size() < 3) {
                throw new IllegalStateException("Please configure at least 3 instances of Mongo for production.");
            }
            mongoOptions.connectionsPerHost = 100;
            this.mongo = new Mongo(mongoAddresses, mongoOptions);
        }
    }

    public void setTestContext(String testContext) {
        this.testContext = Boolean.parseBoolean(testContext);
    }

    public void setMongoAddresses(List<ServerAddress> mongoAddresses) {
        this.mongoAddresses = mongoAddresses;
    }

    public Mongo getMongo() {
        return mongo;
    }
}