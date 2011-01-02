/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.controller;

import eu.delving.core.binding.BriefDocItem;
import eu.delving.core.binding.FacetStatisticsMap;
import eu.delving.core.binding.SolrBindingService;
import eu.europeana.core.querymodel.query.QueryModelFactory;
import eu.europeana.core.querymodel.query.SolrQueryUtil;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Where people arrive.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 */

@Controller
public class IndexPageController {

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    private QueryModelFactory beanQueryModelFactory;

    @RequestMapping("/index.html")
    public ModelAndView indexHandler(HttpServletRequest request) throws Exception {
        final ModelAndView page = ControllerUtil.createModelAndViewPage("index_orig");
        final SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addSortField(SolrQueryUtil.createRandomSortKey(), SolrQuery.ORDER.asc);
        solrQuery.addFilterQuery("europeana_hasDigitalObject:true");
        solrQuery.setFields("europeana_uri", "europeana_object", "DATAPROVIDER", "TYPE", "title", "creator");
        solrQuery.setRows(10);
        final QueryResponse solrResponse = beanQueryModelFactory.getSolrResponse(solrQuery);
        final List<BriefDocItem> briefDocs = SolrBindingService.getBriefDocs(solrResponse);
        page.addObject("randomItems", briefDocs);
        final SolrQuery statsQuery = new SolrQuery("*:*");
        statsQuery.setFacet(true);
        statsQuery.setFacetMinCount(1);
        statsQuery.setFacetLimit(100);
        statsQuery.addFacetField("DATAPROVIDER", "COUNTY", "HASDIGITALOBJECT");
        statsQuery.setRows(0);
        final QueryResponse statsResponse = beanQueryModelFactory.getSolrResponse(solrQuery);
        final List<FacetField> facetFields = statsResponse.getFacetFields();
        final FacetStatisticsMap facetStatistics = SolrBindingService.createFacetStatistics(facetFields);
        page.addObject("facetMap", facetStatistics);
        page.addObject("totalCount", statsResponse.getResults().getNumFound());
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.INDEXPAGE, page);
        return page;
    }
}
