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

package eu.europeana.core.database.incoming.cli;

import eu.europeana.core.database.DashboardDao;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.ImportFileState;
import eu.europeana.core.database.incoming.ESEImporter;
import eu.europeana.core.database.incoming.ImportFile;
import eu.europeana.core.database.incoming.ImportRepository;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
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
    private ESEImporter eseImporter;
    private DashboardDao dashboardDao;
    private ImportRepository repository;
    private List<Job> jobs = new ArrayList<Job>();
    private int simultaneousJobs = 5;

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

    public void init() throws ClassNotFoundException {
//        for (String path : System.getProperty("java.class.path").split(":")) {
//            LOG.info("Path: "+path);
//        }
        Class.forName("org.springframework.security.web.FilterChainProxy");
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/core-application-context.xml"
        });
        eseImporter = (ESEImporter) context.getBean("normalizedEseImporter");
        dashboardDao = (DashboardDao) context.getBean("dashboardDao");
        repository = (ImportRepository) context.getBean("normalizedImportRepository");
    }

    public void setSimultaneousJobs(int simultaneousJobs) {
        this.simultaneousJobs = simultaneousJobs;
    }

    public void addMetadataFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            jobs.add(new Job(file));
        }
        else {
            throw new IllegalArgumentException("File [" + fileName + "] does not exist!");
        }
    }

    public void loadMetadata() throws IOException, InterruptedException, SolrServerException {
        LOG.info("simultaneousJobs=" + simultaneousJobs);
        List<Job> activeJobs = new ArrayList<Job>();
        while (!jobs.isEmpty() || !activeJobs.isEmpty()) {
            while (activeJobs.size() < simultaneousJobs && !jobs.isEmpty()) {
                Job job = jobs.remove(0);
                ImportFile importFile = repository.copyToUploaded(job.file);
                job.collection = dashboardDao.fetchCollection(importFile.deriveCollectionName(), importFile.getFileName(), true);
                importFile = eseImporter.commenceImport(importFile, job.collection.getId());
                LOG.info(String.format("Importing commenced for %s", importFile));
                activeJobs.add(job);
            }
            Thread.sleep(10000);
            Iterator<Job> importingJobsWalk = activeJobs.iterator();
            while (importingJobsWalk.hasNext()) {
                Job job = importingJobsWalk.next();
                if (job.isFinished()) {
                    importingJobsWalk.remove();
                }
                else {
                    LOG.debug(String.format("Busy importing %s", job.collection));
                }
            }
            LOG.info(MessageFormat.format("jobs={0}, importingJobs={1}", jobs.size(), activeJobs.size()));
        }
        LOG.info("Finished importing, committing Solr");
        eseImporter.commit();
        LOG.info("Committed Solr");
    }

    private static boolean isSorlRunning() {
        try {
            SolrServer server = new CommonsHttpSolrServer("http://localhost:8983/solr/");
            SolrPingResponse response = server.ping();
            return response.getResponse().get("status").toString().equalsIgnoreCase("ok");
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void main(String... commandLine) throws Exception {
        if (!isSorlRunning()) {
            throw new Exception("Expected to find SOLR running.");
        }
        if (commandLine.length == 0) {
            File file = new File("core/src/test/sample-metadata/92001_Ag_EU_TELtreasures.xml");
            if (file.exists()) {
                throw new Exception("Parameters: XML input files, try " + file.getAbsolutePath());
            }
            else {
                throw new Exception("Parameters missing: XML input files");
            }
        }
        else {
            ContentLoader contentLoader = new ContentLoader();
            for (String command : commandLine) {
                if (command.startsWith("-")) {
                    int simultaneousJobs = Integer.parseInt(command.substring(1));
                    contentLoader.setSimultaneousJobs(simultaneousJobs);
                }
                else {
                    contentLoader.addMetadataFile(command);
                }
            }
            contentLoader.init();
            LOG.info("start loading content");
            contentLoader.loadMetadata();
            LOG.info("finished loading content");
        }
    }
}
