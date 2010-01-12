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

package eu.europeana.query;

import eu.europeana.beans.IdBean;
import eu.europeana.beans.views.BriefBeanView;
import eu.europeana.beans.views.FullBeanView;
import eu.europeana.beans.views.GridBrowseBeanView;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public interface NewQueryModelFactory {

    SolrQuery createFromQueryParams(Map<String, String[]> params) throws EuropeanaQueryException;

    SolrQuery createFromUri(String europeanaUri);

    QueryResponse getSolrResponse(SolrQuery solrQuery, Class<?> beanClass) throws EuropeanaQueryException;

    BriefBeanView getBriefResultView(SolrQuery solrQuery, String requestQueryString) throws EuropeanaQueryException, UnsupportedEncodingException;

    FullBeanView getFullResultView(SolrQuery solrQuery, Map<String, String[]> params) throws EuropeanaQueryException;

    FullDoc getFullDoc(SolrQuery solrQuery) throws EuropeanaQueryException;

    GridBrowseBeanView getGridBrowseResultView(SolrQuery solrQuery) throws EuropeanaQueryException;

    List<IdBean> getDocIdList(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException;

    QueryResponse getSolrResponse(SolrQuery solrQuery) throws EuropeanaQueryException;
}