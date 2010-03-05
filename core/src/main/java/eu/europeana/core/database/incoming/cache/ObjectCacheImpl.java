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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
    private static final String[] HEX = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private final MessageDigest digest;
    private final Logger log = Logger.getLogger(getClass());

    private Map<String, MimeType> extensionMap = new HashMap<String, MimeType>();
    private File root;

    public ObjectCacheImpl() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
        for (MimeType t : MimeType.values()) {
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
        return new File(root, collection.getName() + ".sh");
    }

    @Override
    public String createFetchScriptBegin(String collectionName) {
        StringBuilder out = new StringBuilder();
        out.append("#!/usr/bin/env sh\n");
        out.append("\n");
        out.append("# Running this script file should fetch the original digital objects\n");
        out.append("# and place them in the " + ItemSize.ORIGINAL + " directory\n");
        out.append("\n");
        out.append("# First remove output from any previous run\n");
        out.append("rm ").append(collectionName).append(".txt\n");
        out.append("\n");
        out.append("# Then fetch the names and log them\n");
        out.append("\n");
        return out.toString();
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
        String cacheString = createHash(objectUri);
        String directoryString = getDirectory(cacheString);
        String filePath = File.separator + directoryString + File.separator + cacheString;
        StringBuilder out = new StringBuilder();
        // the wget
        out.append("wget -q -O ")
                .append(root).append(File.separator).append(ItemSize.ORIGINAL).append(filePath)
                .append(' ')
                .append(objectUri).append('\n');
        out.append("echo \"").append(europeanaUri).append("\" \"").append(objectUri).append("\" \"").append(filePath).append("\" $? >> ").append(collectionName).append(".txt\n");
        out.append("echo \"").append(objectUri).append("\"\n");
        out.append("echo \"").append(ItemSize.ORIGINAL).append(filePath).append("\"\n");
        // then the convert to full doc
        out.append("convert  ")
                .append(root).append(File.separator).append(ItemSize.ORIGINAL).append(filePath).append(" -thumbnail \"200x>\" ")
                .append(root).append(File.separator).append(ItemSize.FULL_DOC).append(filePath).append('\n');
        out.append("echo \"").append(ItemSize.FULL_DOC).append(filePath).append("\"\n");
        // then reduce again to brief doc
        out.append("convert  ")
                .append(root).append(File.separator).append(ItemSize.FULL_DOC).append(filePath).append(" -thumbnail \"x110\" ")
                .append(root).append(File.separator).append(ItemSize.BRIEF_DOC).append(filePath).append('\n');
        out.append("echo \"").append(ItemSize.BRIEF_DOC).append(filePath).append("\"\n");
        out.append('\n');
        return out.toString();
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

    private File getCacheRoot(File root, ItemSize itemSize, String hash) {
        if (!root.exists()) {
            try {
                createCacheDirectories(root);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to create cache directories", e);
            }
        }
        File itemSizeRoot = new File(root, itemSize.toString());
        File cacheRoot = new File(itemSizeRoot, getDirectory(hash));
        if (!cacheRoot.exists()) { // this should never happen
            throw new RuntimeException("Cache root should already have been created: " + cacheRoot.getAbsolutePath());
        }
        return cacheRoot;
    }

    private static String getDirectory(String hash) {
        return hash.substring(0, 2) + File.separator + hash.substring(2, 4);
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
        for (ItemSize itemSize : ItemSize.values()) {
            for (String dirA : HEX) {
                for (String dirB : HEX) {
                    for (String dirC : HEX) {
                        for (String dirD : HEX) {
                            String directoryString = String.format("%s%s%s%s%s", dirA, dirB, File.separator, dirC, dirD);
                            FileUtils.forceMkdir(new File(root, String.format("%s%s%s", itemSize, File.separator, directoryString)));
                            count++;
                        }
                    }
                    count++;
                }
            }
        }
        return count;
    }

//    private int createOldStyleCacheDir(File root) throws IOException {
//        int count = 0;
//        for (String dirA : DIRECTORY_CHARS) {
//            for (String dirB : DIRECTORY_CHARS) {
//                for (String dirC : DIRECTORY_CHARS) {
//                    String directoryString = String.format("%s%s%s", dirA, dirB, dirC);
//                    FileUtils.forceMkdir(new File(root.toString() + File.separator + directoryString));
//                    count++;
//                }
//            }
//        }
//        return count;
//    }
//
//    private int createThreeTypeCacheDir(File root) throws IOException {
//        int count = 0;
//        for (String type : TYPES) {
//            for (String dirA : DIRECTORY_CHARS) {
//                for (String dirB : DIRECTORY_CHARS) {
//                    for (String dirC : DIRECTORY_CHARS) {
//                        String directoryString = String.format("%s%s%s", dirA, dirB, dirC);
//                        System.out.println(directoryString);
//                        FileUtils.forceMkdir(new File(root.toString() + File.separator + type + File.separator + directoryString));
//                        count++;
//                    }
//                }
//            }
//        }
//        return count;
//    }
}
