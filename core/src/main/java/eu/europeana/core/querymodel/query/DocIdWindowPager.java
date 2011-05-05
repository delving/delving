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

import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.RecordDefinition;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;

import java.util.List;
import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 9, 2010 12:47:37 PM
 */
public interface DocIdWindowPager {
    DocIdWindow getDocIdWindow();

    boolean isNext();

    boolean isPrevious();

    String getQueryStringForPaging();

    String getFullDocUri();

    String getNextFullDocUrl();

    String getPreviousFullDocUrl();

    String getNextUri();

    int getNextInt();

    String getPreviousUri();

    int getPreviousInt();

    String getQuery();

    String getReturnToResults();

    String getPageId();

    String getTab();

    String toString();

    String getStartPage();

    List<Breadcrumb> getBreadcrumbs();

    int getNumFound();

    int getFullDocUriInt();

    void setPortalName(String portalName);

    void initialize(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery, QueryModelFactory queryModelFactory, RecordDefinition metadataModel) throws SolrServerException, EuropeanaQueryException;

    String getSortBy();
}
