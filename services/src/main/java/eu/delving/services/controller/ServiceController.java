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

import eu.delving.services.cache.image.ImageCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Value("#{launchProperties['displayPageUrl']}")
    private String displayPageUrl;

    @Value("#{launchProperties['resolverUrlPrefix']}")
    private String resolverUrlPrefix;

    @Autowired
    private ImageCacheService imageCacheService;

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
                report(response, "Expected path like /resolve/object/*/*");
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

    @RequestMapping("/image")
    public void imageProxy(
            HttpServletResponse response,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "id", required = false) String url,
            @RequestParam(value = "size", required = false) String sizeString
    ) throws IOException {
        imageCacheService.retrieveImageFromCache(url, response);
    }

    private static void report(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Resolver</title></head><body><h2>");
        out.println(message);
        out.println("</body></html></h2>");
    }
}
