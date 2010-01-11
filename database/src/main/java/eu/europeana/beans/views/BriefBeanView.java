package eu.europeana.beans.views;

import eu.europeana.beans.EuropeanaView;
import eu.europeana.query.BriefDoc;
import eu.europeana.query.ResultPagination;
import eu.europeana.web.util.FacetQueryLinks;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
* @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
* @since Jan 9, 2010 11:51:46 AM
*/
@EuropeanaView(facets = true, rows = 10) // todo: this might be the right place for this annotation
public interface BriefBeanView {
    List<? extends BriefDoc> getBriefDocs();
    List<FacetQueryLinks> getQueryFacetsLinks() throws UnsupportedEncodingException;
    ResultPagination getPagination();
}
