/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

import eu.delving.services.cache.ItemSize;
import eu.europeana.core.querymodel.query.DocType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

/**
 * Controller for chache and resolver
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */


@Controller
public class ServiceController {
    private Logger log = Logger.getLogger(getClass());
    private static final String DEFAULT_IMAGE = "/cache/unknown.png";
    private static final String[][] TYPES = {
            {ItemSize.BRIEF_DOC.toString(), DocType.TEXT.toString(), "/cache/item-page.gif"},
            {ItemSize.BRIEF_DOC.toString(), DocType.IMAGE.toString(), "/cache/item-image.gif"},
            {ItemSize.BRIEF_DOC.toString(), DocType.SOUND.toString(), "/cache/item-sound.gif"},
            {ItemSize.BRIEF_DOC.toString(), DocType.VIDEO.toString(), "/cache/item-video.gif"},
            {ItemSize.FULL_DOC.toString(), DocType.TEXT.toString(), "/cache/item-page-large.gif"},
            {ItemSize.FULL_DOC.toString(), DocType.IMAGE.toString(), "/cache/item-image-large.gif"},
            {ItemSize.FULL_DOC.toString(), DocType.SOUND.toString(), "/cache/item-sound-large.gif"},
            {ItemSize.FULL_DOC.toString(), DocType.VIDEO.toString(), "/cache/item-video-large.gif"},
    };
    private final int CACHE_DURATION_IN_SECOND = 60 * 60 * 2; // 2 hour
    private final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND * 1000;

    @Value("#{launchProperties['displayPageUrl']}")
    private String displayPageUrl;

    @Value("#{launchProperties['resolverUrlPrefix']}")
    private String resolverUrlPrefix;

    @RequestMapping("/resolve")
    public void resolve(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            report(response, "Nothing to resolve");
        }
        else {
            String[] parts = pathInfo.split("/");
            if (parts.length != 4 || !"record".equals(parts[1])) {
                report(response, "Expected path like /resolve/record/*/*");
            }
            else {
                String uri = resolverUrlPrefix + request.getRequestURI();
                String encodedUri = URLEncoder.encode(uri, "UTF-8");
                String redirect = displayPageUrl + "?uri=" + encodedUri;
//                report(response, redirect);
                response.sendRedirect(redirect);
            }
        }
    }

    private static void report(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Resolver</title></head><body><h2>");
        out.println(message);
        out.println("</body></html></h2>");
    }
}
