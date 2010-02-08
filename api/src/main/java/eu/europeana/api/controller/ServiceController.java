package eu.europeana.api.controller;

import eu.europeana.cache.ItemSize;
import eu.europeana.cache.MimeType;
import eu.europeana.cache.ObjectCache;
import eu.europeana.query.DocType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @Autowired
    private ObjectCache objectCache;

    @Value("#{europeanaProperties['displayPageUrl']}")
    private String displayPageUrl;

    @Value("#{europeanaProperties['resolverUrlPrefix']}")
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

    @RequestMapping("/image")
    public void cache(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "uri", required = false) String uri
    ) throws IOException {
        if (uri == null) {
            log.warn("Needs 'uri' parameter");
            reportURIRequired(response, type, ItemSize.BRIEF_DOC);
        }
        else {
            ItemSize itemSize = ItemSize.FULL_DOC;
            String sizeString = request.getParameter("size");
            if (sizeString != null) {
                try {
                    itemSize = ItemSize.valueOf(sizeString);
                }
                catch (IllegalArgumentException e) {
                    log.warn("Parameter 'size' is invalid");
                    itemSize = ItemSize.FULL_DOC;
                }
            }
            else {
                log.warn("No 'size' parameter, using " + itemSize);
            }
            File cachedFile = objectCache.getFile(itemSize, uri);
            if (cachedFile == null) {
                reportURINotCached(response, uri, type, itemSize, "Nothing in cache for this URI.");
            }
            else if (!respondWithContent(response, cachedFile)) {
                reportURINotCached(response, uri, type, itemSize, "Cacheing of this URI resulted in an error.");
            }
        }
    }

    @SuppressWarnings({"NestedAssignment"})
    private boolean respondWithContent(HttpServletResponse response, File cachedFile) throws IOException {
        MimeType mimeType = objectCache.getMimeType(cachedFile);
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

    private void report(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Resolver</title></head><body><h2>");
        out.println(message);
        out.println("</body></html></h2>");
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

    /* Sets the HTTP header that instruct cache control to the browser. */

    private void setCacheControlHeaders(HttpServletResponse response) {
        long now = System.currentTimeMillis();
        response.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
        response.addHeader("Cache-Control", "must-revalidate"); //optional
        response.setDateHeader("Last-Modified", now);
        response.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);

    }
}
