/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or as soon they
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

import eu.europeana.query.DocType;
import eu.europeana.query.EuropeanaProperties;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * This servlet delivers the cached versions of the uri.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@SuppressWarnings({"NestedAssignment"})
public class CacheServlet extends HttpServlet {
    private static final long serialVersionUID = -130709210249378651L;
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
    private final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND  * 1000;

    private Logger log = Logger.getLogger(getClass());
    private DigitalObjectCache digitalObjectCache;

    @Override
    public void init(ServletConfig config) throws ServletException {
        EuropeanaProperties properties = new EuropeanaProperties();
        DigitalObjectCacheImpl digitalObjectCache = new DigitalObjectCacheImpl();
        String cacheRoot = properties.getProperty("cache.cacheRoot");
        if (cacheRoot == null) {
            throw new ServletException("Init parameter 'cacheRoot' required.");
        }
        digitalObjectCache.setRoot(new File(cacheRoot));
        String imageMagickPath = properties.getProperty("cache.imageMagickPath");
        if (imageMagickPath == null) {
            throw new ServletException("Init parameter 'imageMagickPath' required.");
        }
        digitalObjectCache.setImageMagickPath(imageMagickPath);
        this.digitalObjectCache = digitalObjectCache;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        String uri = request.getParameter("uri");
        if (uri == null) {
            log.warn("Needs 'uri' parameter");
            reportURIRequired(response, type, ItemSize.BRIEF_DOC);
        }
        else {
            ItemSize size = ItemSize.FULL_DOC;
            String sizeString = request.getParameter("size");
            if (sizeString != null) {
                try {
                    size = ItemSize.valueOf(sizeString);
                }
                catch (IllegalArgumentException e) {
                    log.warn("Parameter 'size' is invalid");
                    size = ItemSize.FULL_DOC;
                }
            }
            else {
                log.warn("No 'size' parameter, using " + size);
            }
            File[] cachedFiles = digitalObjectCache.getItemSizeCachedFiles(uri);
            File cachedFile = null;
            if (cachedFiles != null) {
                cachedFile = cachedFiles[size.ordinal()];
            }
            if (cachedFile == null) {
                reportURINotCached(response, uri, type, size, "Nothing in cache for this URI.");
            }
            else {
                if (!respondWithContent(response, cachedFile)) {
                    reportURINotCached(response, uri, type, size, "Cacheing of this URI resulted in an error.");
                }
            }
        }
    }

    @SuppressWarnings({"NestedAssignment"})
    private boolean respondWithContent(HttpServletResponse response, File cachedFile) throws IOException {
        MimeType mimeType = digitalObjectCache.getMimeType(cachedFile);
        if (mimeType == MimeType.ERROR) {
            return false;
        }
        response.setContentType(mimeType.getType());
        setCacheControlHeaders(response);
        OutputStream out = response.getOutputStream();
        InputStream in = new FileInputStream(cachedFile);
        try {
            int bytes;
            byte[] buffer = new byte[2048];
            while ((bytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytes);
            }
        }
        finally {
            in.close();
            out.close();
        }
        return true;
    }

    private void reportURIRequired(HttpServletResponse response, String type, ItemSize size) throws IOException {
        if (type == null) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html><head><title>Cache Servlet</title></head><body>");
            out.println("<h1>URI parameter required!</h1>");
            out.println("</body></html>");
        }
        else {
            respondWithDefaultImage(response, type, size);
        }
    }

    private void reportURINotCached(HttpServletResponse response, String uri, String type, ItemSize size, String reason) throws IOException {
        if (type == null) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html><head><title>Cache Servlet</title></head><body>");
            out.println("<h1>URI not cached!</h1>");
            out.println("<pre>");
            out.println(uri);
            out.println("</pre>");
            out.println("<font color='red'>");
            out.println("Reason: " + reason);
            out.println("</font>");
            out.println("</body></html>");
        }
        else {
            respondWithDefaultImage(response, type, size);
        }
    }

    private void respondWithDefaultImage(HttpServletResponse response, String type, ItemSize size) throws IOException {
        response.setContentType("image/png");
        setCacheControlHeaders(response);
        String resource = DEFAULT_IMAGE;
        for (String[] array : TYPES) {
            if (array[0].equals(size.toString()) && array[1].equalsIgnoreCase(type)) {
                resource = array[2];
            }
        }
        InputStream in = getClass().getResourceAsStream(resource);
        OutputStream out = response.getOutputStream();
        try {
            int bytes;
            byte[] buffer = new byte[2048];
            while ((bytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytes);
            }
        }
        finally {
            in.close();
            out.close();
        }
    }

    /** Sets the HTTP header that instruct cache control to the browser. */
    private void setCacheControlHeaders(HttpServletResponse response) {
        long now = System.currentTimeMillis();
        response.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
        response.addHeader("Cache-Control", "must-revalidate"); //optional
        response.setDateHeader("Last-Modified", now);
        response.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);

    }
}
