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

import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.ImportFileState;
import eu.europeana.incoming.ESEImporter;
import eu.europeana.incoming.ImportFile;
import eu.europeana.incoming.ImportRepository;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Load content into the index for the purposes of testing
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ContentLoader {
    private static final Logger LOG = Logger.getLogger(ContentLoader.class);
    private final ESEImporter eseImporter;
    private final DashboardDao dashboardDao;
    private final ImportRepository repository;
    private List<Job> jobs = new ArrayList<Job>();

    private class Job {
        EuropeanaCollection collection;
        File file;

        private Job(File file) {
            this.file = file;
        }

        public boolean isFinished() {
            collection = dashboardDao.fetchCollection(collection.getId());
            return collection.getFileState() == ImportFileState.IMPORTED || collection.getFileState() == ImportFileState.ERROR;
        }
    }

    public ContentLoader() {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/core-application-context.xml"
        });
        eseImporter = (ESEImporter) context.getBean("normalizedEseImporter");
        dashboardDao = (DashboardDao) context.getBean("dashboardDao");
        repository = (ImportRepository) context.getBean("normalizedImportRepository");
    }

    public void addMetadataFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            jobs.add(new Job(file));
        }
        else {
            throw new IllegalArgumentException("File ["+fileName+"] does not exist!");
        }
    }

    public void loadMetadata() throws IOException, InterruptedException, SolrServerException {
        for (Job job : jobs) {
            job.collection = dashboardDao.fetchCollectionByFileName(job.file.getName());
            ImportFile importFile = repository.copyToUploaded(job.file);
            if (job.collection == null) {
                job.collection = dashboardDao.fetchCollectionByName(importFile.getFileName(), true);
            }
            importFile = eseImporter.commenceImport(importFile, job.collection.getId());
            LOG.info(String.format("Importing commenced for %s", importFile));
        }
        while (!jobs.isEmpty()) {
            Thread.sleep(5000);
            Iterator<Job> importingFileWalk = jobs.iterator();
            while (importingFileWalk.hasNext()) {
                Job job = importingFileWalk.next();
                if (job.isFinished()) {
                    importingFileWalk.remove();
                }
                else {
                    LOG.info(String.format("Busy importing %s", job.collection));
                }
            }
        }
        LOG.info("Finished importing, committing Solr");
        eseImporter.commit();
        LOG.info("Committed Solr");
    }

    public static void main(String... commandLine) throws Exception {
        ContentLoader contentLoader = new ContentLoader();
        if (commandLine.length == 0) {
            contentLoader.addMetadataFile("./core/src/test/resources/test-files/92001_Ag_EU_TELtreasures.xml");
        }
        else for (String fileName : commandLine) {
            contentLoader.addMetadataFile(fileName);
        }
        LOG.info("Starting Solr Server");
        SolrStarter solrStarter = new SolrStarter();
        solrStarter.start();
        contentLoader.loadMetadata();
        LOG.info("Stopping Solr Server");
        Thread.sleep(10000);
        solrStarter.stop();
    }
}
