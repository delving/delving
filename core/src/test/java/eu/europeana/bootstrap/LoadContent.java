/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.bootstrap;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.europeana.database.DashboardDao;
import eu.europeana.database.LanguageDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.ImportFileState;
import eu.europeana.database.migration.DataMigration;
import eu.europeana.incoming.ESEImporter;
import eu.europeana.incoming.ImportFile;
import eu.europeana.incoming.ImportRepository;


/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>, Borys Omelayenko
 * @since Jun 29, 2009: 4:15:22 PM
 */
public class LoadContent {
    private static final Logger log = Logger.getLogger(LoadContent.class);

    ESEImporter eseImporter;
    DashboardDao dashboardDao;
    ImportRepository repository;
    LanguageDao languageDao;
    StaticInfoDao staticInfoDao;
    protected UserDao userDao;

    public static void main(String... args) throws Exception {

    	LoadContent contentLoader = new LoadContent();
    	contentLoader.init();
    	contentLoader.load((args.length == 0) || !args[0].equalsIgnoreCase("skip=true"));
    }

    public void init() throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/core-application-context.xml"
        });

        eseImporter = (ESEImporter) context.getBean("normalizedEseImporter");
        dashboardDao = (DashboardDao) context.getBean("dashboardDao");
        repository = (ImportRepository) context.getBean("normalizedImportRepository");
        languageDao = (LanguageDao) context.getBean("languageDao");
        staticInfoDao = (StaticInfoDao) context.getBean("staticInfoDao");
        userDao = (UserDao) context.getBean("userDao");
    }

    public void postLoad() {
    	
    }
    
	public UserDao getUserDao() {
		return userDao;
	}

	public void load(boolean loadStatic)
			throws IOException, Exception, InterruptedException {
		// load static content etc.
        if (loadStatic) {
            log.info("Start loading static content.");
            DataMigration migration = new DataMigration();
            migration.setLanguageDao(languageDao);
            migration.setPartnerDao(staticInfoDao);
            migration.readTableFromResource(DataMigration.Table.CONTRIBUTORS);
            migration.readTableFromResource(DataMigration.Table.PARTNERS);
            migration.readTableFromResource(DataMigration.Table.TRANSLATION_KEYS);
            migration.readTableFromResource(DataMigration.Table.STATIC_PAGE);
            log.info("Finished loading static content.");
        }


        // import and index sample records
        SolrStarter solr = new SolrStarter();
        log.info("Starting Solr Server");
        solr.start();


        final File file = new File("./core/src/test/resources/test-files/92001_Ag_EU_TELtreasures.xml");
        EuropeanaCollection europeanaCollection = dashboardDao.fetchCollectionByFileName(file.getName());
        ImportFile importFile = repository.copyToUploaded(file);
        if (europeanaCollection == null) {
            europeanaCollection = dashboardDao.fetchCollectionByName(importFile.getFileName(), true);
        }
        importFile = eseImporter.commenceImport(importFile, europeanaCollection.getId());
        log.info("Importing commenced for " + importFile);
        while (europeanaCollection.getFileState() == ImportFileState.UPLOADED) {
            log.info("waiting to leave UPLOADED state");
            Thread.sleep(500);
            europeanaCollection = dashboardDao.fetchCollection(europeanaCollection.getId());
        }
        while (europeanaCollection.getFileState() == ImportFileState.IMPORTING) {
            log.info("waiting to leave IMPORTING state");
            Thread.sleep(500);
            europeanaCollection = dashboardDao.fetchCollection(europeanaCollection.getId());
        }
        Thread.sleep(10000);
        solr.stop();
        log.info("Stopping Solr server");

        postLoad();
	}
}
