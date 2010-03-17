package eu.europeana.api.controller;

import eu.europeana.core.BeanQueryModelFactory;
import eu.europeana.core.querymodel.beans.BriefBean;
import eu.europeana.core.querymodel.query.QueryType;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 17, 2010 3:00:23 PM
 */

@Controller
public class OpenSearchController {

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    private BeanQueryModelFactory beanQueryModelFactory;

    @RequestMapping("/open-search.html")
    public ModelAndView searchController(HttpServletRequest request) throws Exception {
        // create model and view
        ModelAndView mav = ControllerUtil.createModelAndViewPage("open-search.xml");
        // get open search parameters from uri +  convert them to Lucene query language
        String convertLuceneQuery = convertOpenSearchQueryToLuceneQuery(request);
        // extract human readible query, this is the open search query (not the luceneQuery)
        String queryString = "*:*";
        // create solr query
        SolrQuery solrQuery = new SolrQuery()
            .setQuery(convertLuceneQuery)
            .setRows(12)
            .setQueryType(QueryType.ADVANCED_QUERY.toString());
        // get response from solr
        QueryResponse response = beanQueryModelFactory.getSolrResponse(solrQuery);
        // bind response to briefDoc and add model
        List<BriefBean> briefDocList = response.getBeans(BriefBean.class);
        mav.addObject("briefDocList", briefDocList);
        return mav;
    }

    private String convertOpenSearchQueryToLuceneQuery(HttpServletRequest request ) {
        // todo add code here to convert open search query to lucene query
        return "*:*";
    }

}
