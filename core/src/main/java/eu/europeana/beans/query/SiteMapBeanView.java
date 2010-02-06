package eu.europeana.beans.query;

import eu.europeana.beans.IdBean;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 6, 2010 1:39:51 PM
 */
public interface SiteMapBeanView {
    List<IdBean> getIdBeans();
    int getNumFound();
    String getCollectionName();
    int getMaxPageForCollection();
}
