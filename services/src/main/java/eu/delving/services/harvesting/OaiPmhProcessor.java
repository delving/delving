package eu.delving.services.harvesting;

import eu.delving.services.core.MetaRepo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 26, 2010 5:30:39 PM
 */
public interface OaiPmhProcessor {
    String parseHttpServletRequest(HttpServletRequest request, MetaRepo metaRepo);
}
