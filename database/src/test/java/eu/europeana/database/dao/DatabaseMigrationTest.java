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

package eu.europeana.database.dao;

import eu.europeana.database.migration.DataMigration;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 28, 2009: 9:35:58 PM
 */
public class DatabaseMigrationTest {
    private static final Logger log = Logger.getLogger(DatabaseMigrationTest.class);

//    @Autowired
//    private PartnerDao partnerDao;
//
//    @Autowired
//    private MessageDao messageDao;

    private DataMigration migration;

    @Before
    public void setUp() {
        migration = new DataMigration("/tmp/");
    }

    @Test
    public void testExportDatabaseMigration() throws IOException {
        int tablesExported = migration.exportTables();
        Assert.assertEquals(4 , tablesExported);
    }

    @Test
    public void testImportDatabaseMigration() throws IOException {
        int tablesImported = migration.importTables();
        Assert.assertEquals(4, tablesImported);
    }
}
