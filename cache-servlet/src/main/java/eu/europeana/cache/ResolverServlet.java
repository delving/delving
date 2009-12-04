/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.cache;

import eu.europeana.query.EuropeanaProperties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

/**
 * This servlet redirects browsers to the place where the URI can be resolved
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ResolverServlet extends HttpServlet {

    private String displayPageUrl;
    private String resolverUrlPrefix;
    private static final long serialVersionUID = 8761206535739580292L;

    @Override
    public void init(ServletConfig config) throws ServletException {
        EuropeanaProperties properties = new EuropeanaProperties();
        displayPageUrl = properties.getProperty("displayPageUrl");
        if (displayPageUrl == null) {
            throw new ServletException("Requires 'displayPageUrl' init parameter.");
        }
        resolverUrlPrefix = properties.getProperty("resolverUrlPrefix");
        if (resolverUrlPrefix == null) {
            throw new ServletException("Requires 'resolverUrlPrefix' init parameter.");
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    private void report(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Resolver</title></head><body><h2>");
        out.println(message);
        out.println("</body></html></h2>");
    }


}
