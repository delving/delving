package eu.europeana.beans.views;

import eu.europeana.beans.EuropeanaView;
import eu.europeana.query.BriefDoc;
import eu.europeana.query.DocIdWindowPager;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.FullDoc;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 9, 2010 11:58:17 AM
 */
@EuropeanaView(facets = false, rows = 3) // todo: this might be the right place for this annotation
public interface FullBeanView {
    DocIdWindowPager getDocIdWindowPager() throws Exception, UnsupportedEncodingException;
    List<? extends BriefDoc> getRelatedItems();
    FullDoc getFullDoc() throws EuropeanaQueryException;
}
