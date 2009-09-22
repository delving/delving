/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.database.migration.incoming;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 29, 2009: 10:11:17 PM
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"/database-test-application-context.xml", "/test-application-context.xml"})
public class DatabaseImporterTest {
    private static final Logger log = Logger.getLogger(DatabaseImporterTest.class);

    DataImporter importer;

    @Before
    public void init() {
        importer = new DataImporter();
    }

    @Test
    public void testImportFile() {
        String collectionFileName = importer.importFile("./database/src/test/resources/test-files/92001_Ag_EU_TELtreasures.xml");
        Assert.assertEquals("FileName should be equal", "92001_Ag_EU_TELtreasures.xml", collectionFileName);
        importer.prepareCollectionForIndexing(collectionFileName);
        importer.runIndexer();
    }
}
