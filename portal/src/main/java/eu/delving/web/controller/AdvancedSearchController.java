package eu.delving.web.controller;

import eu.delving.web.controller.norvegiana.AdvancedSearchForm;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.QueryModelFactory;
import eu.europeana.core.querymodel.query.QueryType;
import eu.europeana.core.querymodel.query.SolrQueryUtil;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/advancedsearch.html")
public class AdvancedSearchController {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    private QueryModelFactory beanQueryModelFactory;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(HttpServletRequest request
     )throws EuropeanaQueryException {
        ModelAndView mav = ControllerUtil.createModelAndViewPage("advancedsearch");
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(0);
        query.setFacet(true);
        query.setQueryType(QueryType.ADVANCED_QUERY.toString());
        String collectionsName = "europeana_collectionTitle";
        final String abmCounty = "COUNTY";
        final String dataProvider = "DATAPROVIDER";
        final String abmMunicipality = "MUNICIPALITY";
        final String europeanaType = "TYPE";
        query.addFacetField(collectionsName, abmCounty, europeanaType, abmMunicipality, dataProvider);
        QueryResponse response = beanQueryModelFactory.getSolrResponse(query);
        List<FacetField> facetFields = response.getFacetFields();
        for (FacetField facetField : facetFields) {
            if (facetField.getName().equalsIgnoreCase(collectionsName)) {
                mav.addObject("collections", facetField.getValues());
            }
            else if (facetField.getName().equalsIgnoreCase(abmCounty)) {
                mav.addObject("county", facetField.getValues());
            }
            else if (facetField.getName().equalsIgnoreCase(dataProvider)) {
                mav.addObject("dataProviders", facetField.getValues());
            }
            else if (facetField.getName().equalsIgnoreCase(europeanaType)) {
                mav.addObject("type", facetField.getValues());
            }
            else if (facetField.getName().equalsIgnoreCase(abmMunicipality)) {
                mav.addObject("municipality", facetField.getValues());
            }
        }
        mav.addObject("ramdomSortKey", SolrQueryUtil.createRandomSortKey());
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.ADVANCED_SEARCH);
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String post(
            HttpServletRequest request,
            AdvancedSearchForm advancedSearchForm
    ) {
        String queryString = advancedSearchForm.toSolrQuery();
        log.info(advancedSearchForm);
        clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.ADVANCED_SEARCH, advancedSearchForm.toString());
        return "redirect:/brief-doc.html?query=" + queryString;
    }

}
