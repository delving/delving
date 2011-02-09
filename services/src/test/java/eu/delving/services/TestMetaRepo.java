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

package eu.delving.services;

import com.mongodb.Mongo;
import eu.delving.services.core.MetaRepoImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/services-application-context.xml",
        "/core-application-context.xml"
})
public class TestMetaRepo {
    private static final String DB_NAME = "test-meta-repo";

    @Autowired
    private Mongo mongo;

    @Autowired
    private MetaRepoImpl metaRepo;

    @Before
    public void before() throws UnknownHostException {
        metaRepo.setMongoDatabaseName(DB_NAME);
        mongo.dropDatabase(DB_NAME);
    }

    @After
    public void after() {
        mongo.dropDatabase(DB_NAME);
    }

    @Test
    public void test() {
        Assert.fail("No tests");
    }
}
