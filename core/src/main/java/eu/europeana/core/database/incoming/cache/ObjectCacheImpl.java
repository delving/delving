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

package eu.europeana.core.database.incoming.cache;

import eu.europeana.core.database.domain.EuropeanaCollection;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that maps URIs of digital objects on the web
 * and hashes them to locations in a local cache.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ObjectCacheImpl implements ObjectCache {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
    private static final String SLASH = File.separator;
    private static final String[] HEX = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final Style STYLE = Style.PAST;
    private final MessageDigest digest;
    private final Logger log = Logger.getLogger(getClass());
    private HttpClient httpClient;

    private enum Style {
        FUTURE,
        PAST
    }

    private Map<String, MimeType> extensionMap = new HashMap<String, MimeType>();
    private Map<String, MimeType> mimeMap = new HashMap<String, MimeType>();
    private File root;

    public ObjectCacheImpl() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.closeIdleConnections(10000);
        HttpConnectionManagerParams params = connectionManager.getParams();
        params.setDefaultMaxConnectionsPerHost(2);
        params.setMaxTotalConnections(10);
        params.setConnectionTimeout(5000);
        connectionManager.setParams(params);
        httpClient = new HttpClient(connectionManager);
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
        for (MimeType t : MimeType.values()) {
            mimeMap.put(t.getType(), t);
            extensionMap.put(t.getExtension(), t);
        }
    }

    public void setRoot(File root) {
        this.root = root;
        log.info("Root directory: " + this.root.getPath());
        if (!root.exists()) {
            try {
                log.info(createCacheDirectories(root) + " directories created");
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to create cache directories", e);
            }
        }
    }

    @Override
    public File getFetchScriptFile(EuropeanaCollection collection) {
        return new File(root, collection.getName() + ".txt");
    }

    @Override
    public String createFetchScriptBegin(String collectionName) {
        return "# This file contains entries [europeanaUri:objectUri:cacheFile:mimeType]\n";
    }

    @Override
    public String createFetchScriptEnd(String collectionName, int recordCount, int objectCount, long elapsedMillis, int indexErrorCount) {
        String durationString = DurationFormatUtils.formatDurationHMS(elapsedMillis);
        StringBuilder out = new StringBuilder();
        out.append("\n");
        out.append("# Collection: ").append(collectionName);
        out.append(", imported and indexed at ").append(DATE_FORMAT.format(new Date())).append('\n');
        out.append("# Processed ").append(recordCount).append(" records and ");
        out.append(objectCount).append(" cacheable objects in ").append(durationString).append('\n');
        out.append("# with ").append(indexErrorCount).append(" index errors.").append('\n');
        return out.toString();
    }

    @Override
    public String createFetchScriptItem(String collectionName, String europeanaUri, String objectUri) {
        String hash = createHash(objectUri);
        String directoryString = getDirectory(hash, ItemSize.ORIGINAL);
        File originalFile = new File(root, directoryString + SLASH + hash);
        String cleanURI = sanitizeURI(objectUri);
        GetMethod getMethod = createGetMethod(cleanURI);
        MimeType mimeType = fetch(originalFile, getMethod);
        File mimeTypedOriginalFile = new File(originalFile.getParent(), hash + mimeType.getExtension());
        if (!originalFile.renameTo(mimeTypedOriginalFile)) {
            log.warn("Unable to rename file to " + mimeTypedOriginalFile.getAbsolutePath());
            mimeType = null;
        }
        log.info("Successfully cached: " + mimeTypedOriginalFile.getAbsolutePath());
        return europeanaUri + ":" + objectUri + ":" + mimeTypedOriginalFile.getAbsolutePath() + ":" + mimeType + '\n';
    }

    @Override
    public File getFile(ItemSize itemSize, String uri) {
        final String hash = createHash(uri);
        File cacheRoot = getCacheRoot(root, itemSize, hash);
        File[] files = cacheRoot.listFiles(new FileFilter() { // todo: can we avoid listing and go directly?

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(hash);
            }
        });
        if (files.length == 0) {
            return null;
        }
        else {
            return files[0];
        }
    }

    @Override
    public MimeType getMimeType(File cachedFile) {
        int dot = cachedFile.getName().lastIndexOf(".");
        String extension = cachedFile.getName().substring(dot);
        MimeType mt = extensionMap.get(extension);
        if (mt == null) {
            return MimeType.ERROR;
        }
        else {
            return mt;
        }
    }

    MimeType getMimeType(String mimeString) {
        int semi = mimeString.indexOf(";");
        if (semi > 0) {
            mimeString = mimeString.substring(0, semi);
        }
        MimeType mt = mimeMap.get(mimeString);
        if (mt == null) {
            return MimeType.ERROR;
        }
        else {
            return mt;
        }
    }

    private File getCacheRoot(File root, ItemSize itemSize, String hash) {
        if (!root.exists()) {
            try {
                createCacheDirectories(root);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to create cache directories", e);
            }
        }
        File cacheRoot = new File(root, getDirectory(hash, itemSize));
        if (!cacheRoot.exists()) { // this should never happen
            throw new RuntimeException("Cache root should already have been created: " + cacheRoot.getAbsolutePath());
        }
        return cacheRoot;
    }

    private static String getDirectory(String hash, ItemSize itemSize) {
        switch (STYLE) {
            case PAST:
                return itemSize + SLASH + hash.substring(0, 3);
            case FUTURE:
                return itemSize + SLASH + hash.substring(0, 2) + SLASH + hash.substring(2, 4);
            default:
                throw new RuntimeException();
        }
    }

    private String createHash(String uri) {
        byte[] raw;
        synchronized (digest) {
            digest.reset();
            try {
                raw = digest.digest(uri.getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        }
        char[] hex = new char[2 * raw.length];
        int index = 0;
        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX[v >>> 4].charAt(0);
            hex[index++] = HEX[v & 0xF].charAt(0);
        }
        return new String(hex);
    }

    private static int createCacheDirectories(File root) throws IOException {
        int count = 0;
        switch (STYLE) {
            case FUTURE:
                for (ItemSize itemSize : ItemSize.values()) {
                    for (String dirA : HEX) {
                        for (String dirB : HEX) {
                            for (String dirC : HEX) {
                                for (String dirD : HEX) {
                                    String directoryString = String.format("%s%s%s%s%s", dirA, dirB, SLASH, dirC, dirD);
                                    FileUtils.forceMkdir(new File(root, String.format("%s%s%s", itemSize, SLASH, directoryString)));
                                    count++;
                                }
                            }
                            count++;
                        }
                    }
                }
                break;
            case PAST:
                for (ItemSize itemSize : ItemSize.values()) {
                    for (String dirA : HEX) {
                        for (String dirB : HEX) {
                            for (String dirC : HEX) {
                                String directoryString = String.format("%s%s%s%s%s", itemSize.toString(), SLASH, dirA, dirB, dirC);
                                FileUtils.forceMkdir(new File(root.toString() + SLASH + directoryString));
                                count++;
                            }
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException();
        }
        return count;
    }

    private void consumeResponse(File targetFile, GetMethod getMethod) throws IOException {
        InputStream in = getMethod.getResponseBodyAsStream();
        FileOutputStream out = new FileOutputStream(targetFile);
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
    }

    private String sanitizeURI(String origURL) {
        return origURL.replaceAll("&amp;", "&");
    }

    private MimeType fetch(File targetFile, GetMethod getMethod) {
        try {
            int httpStatus = httpClient.executeMethod(getMethod);
            if (httpStatus == HttpStatus.SC_OK) {
                Header mimeTypeHeader = getMethod.getResponseHeader("Content-Type");
                String mimeTypeString = (mimeTypeHeader == null) ? "unknown" : mimeTypeHeader.getValue();
                MimeType mimeType = getMimeType(mimeTypeString);
                if (mimeType.isCacheable()) {
                    consumeResponse(targetFile, getMethod);
                    return mimeType;
                }
                else {
                    log.warn("Unable to fetch, mime-type not cacheable " + mimeTypeString + " ==> " + mimeType);
                }
            }
            else {
                log.warn("Unable to fetch [" + targetFile + "], HTTP Status " + httpStatus);
            }
        }
        catch (Exception e) {
            log.warn("Unable to fetch", e);
        }
        finally {
            // because we use multithreaded connection manager the connection needs to be released manually
            getMethod.releaseConnection();
        }
        return null;
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
                return null;
            }
        }
        return getMethod;
    }
}
