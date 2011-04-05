/*
 * Copyright 2011 DELVING BV
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

package eu.europeana.core.querymodel.query;

import eu.delving.metadata.RecordDefinition;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since May 10, 2010 11:51:57 PM
 */
public class DocIdWindowPagerFactory {

    @Value("#{launchProperties['portal.name']}")
    private String portalName = "portal"; // must be injected later

    @Autowired
    private QueryModelFactory beanQueryModelFactory;

    public DocIdWindowPager getPager(Map<String, String[]> params, SolrQuery solrQuery, RecordDefinition recordDefinition) {
        DocIdWindowPager pager = new DocIdWindowPagerImpl();
        pager.setPortalName(portalName);
        try {
            pager.initialize(params, solrQuery, beanQueryModelFactory, recordDefinition);
        } catch (SolrServerException e) {
            return null; // when there are no results from solr return null
        } catch (EuropeanaQueryException e) {
            return null; // when there are no results from solr return null
        }
        return pager;
    }
}
