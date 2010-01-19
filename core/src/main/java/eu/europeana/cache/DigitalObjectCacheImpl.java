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

package eu.europeana.cache;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Cache digital objects which can be fetched from remote sites
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@SuppressWarnings({"ResultOfMethodCallIgnored", "NestedAssignment", "ValueOfIncrementOrDecrementUsed"})
public class DigitalObjectCacheImpl implements DigitalObjectCache {
    private Logger log = Logger.getLogger(DigitalObjectCacheImpl.class);
    private File root;
    private String imageMagickPath;
    private CacheHash cacheHash = new CacheHash();
    private HttpClient httpClient;

    public DigitalObjectCacheImpl() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.closeIdleConnections(10000);
        HttpConnectionManagerParams params = connectionManager.getParams();
        params.setDefaultMaxConnectionsPerHost(2);
        params.setMaxTotalConnections(10);
        params.setConnectionTimeout(5000);
        connectionManager.setParams(params);

        httpClient = new HttpClient(connectionManager);
    }

    public void setRoot(File root) {
        this.root = root;
        log.info("Root directory: " + this.root.getPath());
        if (!this.root.exists()) {
            log.info("Creating root directory");
            this.root.mkdirs();
        }
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

    public boolean cache(String uri) throws IOException {
        File[] cachedFiles = cacheHash.getItemSizeCachedFiles(root, uri);
        if (cachedFiles != null) {
//            if (log.isDebugEnabled()) log.debug("Already exists in cache: " + uri);
            log.info("Already exists in cache: " + uri);
            return true;
        }
        String cleanURI = sanitizeURI(uri);
        GetMethod getMethod = createGetMethod(cleanURI);
        return getMethod != null && fetch(cleanURI, getMethod);
    }

    private String sanitizeURI(String origURL) {
        return origURL.replaceAll("&amp;", "&");
    }

    public boolean remove(String uri) {
        File[] cachedFiles = cacheHash.getItemSizeCachedFiles(root, uri);
        if (cachedFiles != null) {
            for (File file : cachedFiles) {
                if (!file.delete()) {
                    return false;
                }
            }
            return true;
        }
        else {
            return true;
        }
    }

    public File[] getItemSizeCachedFiles(String uri) {
        File[] found = cacheHash.getItemSizeCachedFiles(root, uri);
        if (found == null) {
            log.warn("Nothing found in cache for [" + uri + "]");
        }
        return found;
    }

    public MimeType getMimeType(File cachedFile) {
        return cacheHash.getMimeType(cachedFile);
    }

    private GetMethod createGetMethod(String uri) {
        GetMethod getMethod;
        try {
            getMethod = new GetMethod(uri);
        }
        catch (Exception e) {
            StringBuilder cleanURI = new StringBuilder(uri.length());
            for (int walk = 0; walk < uri.length(); walk++) {
                char ch = uri.charAt(walk);
                switch (ch) {
                    case '\\':
                        cleanURI.append("%5C");
                        break;
                    case '[':
                        cleanURI.append("%5B");
                        break;
                    case ']':
                        cleanURI.append("%5D");
                        break;
                    case ' ':
                        cleanURI.append("%20");
                        break;
                    default:
                        cleanURI.append(ch);
                }
            }
            try {
                getMethod = new GetMethod(cleanURI.toString());
            }
            catch (IllegalArgumentException ee) {
                log.warn("Unable to fetch, BAD URI [" + uri + "]");
                File file = cacheHash.createOriginalCachedFile(root, uri, MimeType.ERROR);
                try {
                    PrintWriter out = new PrintWriter(new FileWriter(file));
                    out.write("Unable to fetch [" + uri + "]!\n");
                    ee.printStackTrace(out);
                    out.close();
                }
                catch (IOException eee) {
                    // ignore
                }
                return null;
            }
        }
        return getMethod;
    }

    private boolean fetch(String uri, GetMethod getMethod) throws IOException, IllegalArgumentException {
        log.info("Fetching " + uri);
        boolean fetchSuccesful = false;
        try {
            int httpStatus = httpClient.executeMethod(getMethod);
            if (httpStatus == HttpStatus.SC_OK) {
                Header mimeTypeHeader = getMethod.getResponseHeader("Content-Type");
                String mimeTypeString = (mimeTypeHeader == null) ? "unknown" : mimeTypeHeader.getValue();
                MimeType mimeType = cacheHash.getMimeType(mimeTypeString);
                if (mimeType.isCacheable()) {
                    fetchSuccesful = cacheItem(uri, getMethod, mimeType);
                    if (fetchSuccesful) {
                        log.info("Successfully cached: " + uri);
                    }
                    else {
                        log.warn("Unable to cache: " + uri);
                    }
                }
                else {
                    log.warn("Unable to fetch, mime-type not cacheable " + mimeTypeString + " ==> " + mimeType);
                }
            }
            else {
                log.warn("Unable to fetch, HTTP Status " + httpStatus);
                File file = cacheHash.createOriginalCachedFile(root, uri, MimeType.ERROR);
                FileWriter out = new FileWriter(file);
                out.write("Unable to fetch [" + uri + "]!\n");
                out.write("HTTP Status = " + httpStatus + "\n");
                out.close();
            }
        }
        finally {
            // because we use multithreaded connection manager the connection needs to be released manually
            getMethod.releaseConnection();
        }
        return fetchSuccesful;
    }

    private boolean cacheItem(String uri, GetMethod getMethod, MimeType mimeType) throws IOException {
        boolean fetchSuccesful;
        InputStream in = getMethod.getResponseBodyAsStream();
        File file = cacheHash.createOriginalCachedFile(root, uri, mimeType);
        FileOutputStream out = new FileOutputStream(file);
        long length = getMethod.getResponseContentLength();
        if (length < 0) {
            log.warn("Unknown content length!");
        }
        try {
            byte[] buffer = new byte[2048];
            int bytes;
            while ((bytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytes);
            }
        }
        finally {
            out.close();
            in.close();
        }
        if (mimeType == MimeType.PDF) {
            file = extractPDFImage(file);
        }
        createThumbnail(ItemSize.FULL_DOC, file);
        createThumbnail(ItemSize.BRIEF_DOC, file);
        file.delete(); // delete the original
        fetchSuccesful = true;
        return fetchSuccesful;
    }

    private File extractPDFImage(File file) throws IOException {
        log.info("Mime type is PDF so extracting a thumbnail with ImageMagick");
        File directory = file.getParentFile();
        String pdfFileName = file.getName();
        String pngFileName = pdfFileName.substring(0, pdfFileName.indexOf(".")) + ".png";
        File pngFile = new File(file.getParentFile(), pngFileName);
        executeImageMagick(directory, pdfFileName + "[0]", pngFileName);
        log.info("Deleting " + file.getAbsolutePath());
        file.delete();
        return pngFile;
    }

    private void createThumbnail(ItemSize size, File file) throws IOException {
        File directory = file.getParentFile();
        String[] parts = file.getName().split("\\.");
        if (parts.length != 2) {
            throw new IOException("Expected 2-part file name");
        }
        String targetFileName = parts[0] + "." + size.toString() + "." + parts[1];
        switch (size) {
            case FULL_DOC:
                executeImageMagick(directory, file.getName(), "-thumbnail", "200x>", targetFileName);
                break;
            case BRIEF_DOC:
                executeImageMagick(directory, file.getName(), "-thumbnail", "x110", targetFileName);
                break;
        }
    }

    private void executeImageMagick(File directory, String... arguments) throws IOException {
        String[] command = new String[arguments.length + 1];
        int index = 0;
        command[index++] = imageMagickPath + "/convert";
        for (String argument : arguments) {
            command[index++] = argument;
        }
        StringBuilder commandString = new StringBuilder();
        for (String part : command) {
            commandString.append(" ").append(part);
        }
        String[] vars = {"PATH=" + imageMagickPath};
        Process process = Runtime.getRuntime().exec(command, vars, directory);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info(line);
        }
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            log.warn(line);
        }
        try {
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new IOException("Unable to run ImageMagick's convert!");
            }
        }
        catch (InterruptedException e) {
            log.error("Wait-for interrupted", e);
        }
        reader.close();
        process.destroy();
    }
}