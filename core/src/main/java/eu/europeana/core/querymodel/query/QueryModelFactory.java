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

package eu.europeana.core.querymodel.query;

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

public interface QueryModelFactory {

    SolrQuery createFromQueryParams(Map<String, String[]> params) throws EuropeanaQueryException;

    SolrQuery createFromUri(String europeanaUri) throws EuropeanaQueryException;

    QueryResponse getSolrResponse(SolrQuery solrQuery, Class<?> beanClass) throws EuropeanaQueryException;

    BriefBeanView getBriefResultView(SolrQuery solrQuery, String requestQueryString) throws EuropeanaQueryException, UnsupportedEncodingException;

    FullBeanView getFullResultView(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException;

    FullDoc getFullDoc(SolrQuery solrQuery) throws EuropeanaQueryException;

    List<?> getDocIdList(Map<String, String[]> params) throws EuropeanaQueryException, SolrServerException;

    QueryResponse getSolrResponse(SolrQuery solrQuery) throws EuropeanaQueryException;

    SiteMapBeanView getSiteMapBeanView(String europeanaCollectionName, int rowsReturned, int pageNumber) throws EuropeanaQueryException, SolrServerException;
}