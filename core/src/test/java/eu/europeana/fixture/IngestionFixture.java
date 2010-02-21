/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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
package eu.europeana.fixture;

import eu.europeana.core.BeanQueryModelFactory;
import eu.europeana.core.database.DashboardDao;
import eu.europeana.core.database.incoming.ESEImporter;
import eu.europeana.core.database.incoming.ESEImporterImpl;
import eu.europeana.core.database.incoming.ImportRepository;
import eu.europeana.core.database.incoming.ImportRepositoryImpl;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.FullDoc;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

/**
 * This is a class which is to help with setting up the test environment for
 * ingestion process testing.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class IngestionFixture {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private BeanQueryModelFactory beanQueryModelFactory;

    @Autowired
    private SolrServer solrServer;

    @Autowired
    private ESEImporterImpl eseImporter;

    private Server server;
    private ImportRepositoryImpl importRepository;

    public ImportRepository getImportRepository() throws IOException {
        if (importRepository == null) {
            importRepository = new ImportRepositoryImpl();
            importRepository.setDataDirectory(findRepository().getAbsolutePath());
            String treasures = IngestionFixture.class.getResource("/test-files/92001_Ag_EU_TELtreasures.xml").getFile();
            importRepository.copyToUploaded(new File(treasures));
        }
        return importRepository;
    }

    public ESEImporter getESEImporter() throws IOException {
        eseImporter.setImportRepository(getImportRepository());
        return eseImporter;
    }

    public FullDoc queryFullDoc(String uri) throws EuropeanaQueryException {
        SolrQuery solrQuery = beanQueryModelFactory.createFromUri(uri);
        return beanQueryModelFactory.getFullDoc(solrQuery);
    }

    public void deleteImportRepository() {
        delete(findRepository());
        importRepository = null;
    }

    public void startSolr() throws Exception {
        delete(findSolrData());
        String solrHome = findRoot().getAbsolutePath()+"/src/test/solr/home";
        String solrData = findTarget().getAbsolutePath()+"/solrdata";
        System.setProperty("solr.solr.home", solrHome);
        System.setProperty("solr.data.dir", solrData);
        server = new Server(8983);
        server.addHandler(new WebAppContext(findRoot().getAbsolutePath()+"/src/test/solr/solr.war", "/solr"));
        server.start();
    }

    public void stopSolr() throws Exception {
        server.stop();
    }

    // --- private parts

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                delete(sub);
            }
        }
        if (!file.delete()) {
            log.info("Unable to delete "+file.getAbsolutePath());
        }
    }

    private File findSolrData() {
        return new File(findTarget(),"solrdata");
    }

    private File findRepository() {
        return new File(findTarget(),"repository");
    }

    private File findTarget() {
        return new File(findRoot(), "target");
    }

    private File findRoot() {
        File target = new File("src");
        if (target.exists()) {
            return new File(".");
        }
        else {
            return new File("core");
        }
    }

}
