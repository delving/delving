/*
 * Copyright 2010 DELVING BV
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

package eu.delving.services.controller;

import eu.delving.services.search.OpenSearchService;
import eu.delving.services.search.RichSearchAPIServiceFactory;
import eu.europeana.core.BeanQueryModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

/**
 * todo: take another good look at this
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 17, 2010 3:00:23 PM
 */

@Controller
public class SearchApiController {

    @Autowired
    private BeanQueryModelFactory beanQueryModelFactory;

    @Autowired
    private Properties launchProperties;

    @Autowired
    private RichSearchAPIServiceFactory richApiServiceFactory;

    @RequestMapping("/api/search")
    public void searchApiController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(richApiServiceFactory.getApiResponse(request, response));
        response.getWriter().close();
    }

    @RequestMapping("/api/open-search")
    public void openSearchServiceController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(OpenSearchService.parseHttpServletRequest(request, beanQueryModelFactory, launchProperties));
        response.getWriter().close();
    }

    @RequestMapping("/api/open-search.xml")
    public void openSearchServiceDescriptionController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(OpenSearchService.renderDescriptionDocument(request, beanQueryModelFactory, launchProperties));
        response.getWriter().close();
    }
}
