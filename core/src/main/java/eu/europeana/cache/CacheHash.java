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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that maps URIs of digital objects on the web
 * and hashes them to locations in a local cache.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@SuppressWarnings({"AssignmentToMethodParameter", "ResultOfMethodCallIgnored", "ValueOfIncrementOrDecrementUsed"})
public class CacheHash {
    private Map<String, MimeType> mimeMap = new HashMap<String, MimeType>();
    private Map<String, MimeType> extensionMap = new HashMap<String, MimeType>();
    private final MessageDigest digest;
    private final String[] type_entries = {"ORIGINAL", "BRIEF_DOC", "FULL_DOC"};
    private final String[] dir_entries = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public CacheHash() {
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

    MimeType getMimeType(File file) {
        int dot = file.getName().lastIndexOf(".");
        String extension = file.getName().substring(dot);
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

    File[] getItemSizeCachedFiles(File root, String uri) {
        final String hash = createHash(uri);
        File cacheRoot = getCacheRoot(root, hash);
        File[] files = cacheRoot.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getName().startsWith(hash);
            }
        });
        if (files.length == 0) {
            return null;
        }
        File[] sizeFiles = new File[ItemSize.values().length];
        for (ItemSize size : ItemSize.values()) {
            sizeFiles[size.ordinal()] = fetchFile(size, files);
        }
        return sizeFiles;
    }

    File createOriginalCachedFile(File root, String uri, MimeType mimeType) {
        String hash = createHash(uri);
        File cacheRoot = getCacheRoot(root, hash);
        return new File(cacheRoot, hash + mimeType.getExtension());
    }

    private File fetchFile(ItemSize size, File[] files) {
        for (File file : files) {
            if (file.getName().indexOf(size.toString()) > 0) {
                return file;
            }
        }
        return null;
    }

    private File getCacheRoot(File root, String hash) {
//        if (!root.exists()) {
//            createNewStyleCacheDir(root);
//        }
        File cacheRoot = new File(root, getDirectory(hash));
        if (!cacheRoot.exists()) { // this should never happen
            cacheRoot.mkdirs();
        }
        return cacheRoot;
    }

    public String getDirectory(String hash) {
        return hash.substring(0, 2) + File.separator + hash.substring(2, 4);
    }

    public String createHash(String uri) {
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
        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        try {
            return new String(hex, "ASCII");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    private static final byte[] HEX_CHAR_TABLE = {
            (byte) '0', (byte) '1', (byte) '2', (byte) '3',
            (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'
    };

    void createNewStyleCacheDir(File root) throws IOException {
        int counter = 0;
        for (String type_entry : type_entries) {
            for (String first_entry : dir_entries) {
                for (String second_entry : dir_entries) {
                    for (String third_entry : dir_entries) {
                        for (String fourth_entry : dir_entries) {
                            String directoryString = String.format("%s%s%s%s%s", first_entry, second_entry, File.separator, third_entry, fourth_entry);
                            FileUtils.forceMkdir(new File(root.toString() + File.separator + type_entry + File.separator + directoryString));
                            counter++;
                        }
                    }
                    counter++;
                }
            }
        }
        System.out.println("directories created: " + counter);
    }

    private void createOldStyleCacheDir(File root) throws IOException {
        int counter = 0;
        for (String first_entry : dir_entries) {
            for (String second_entry : dir_entries) {
                for (String third_entry : dir_entries) {
                    String directoryString = String.format("%s%s%s", first_entry, second_entry, third_entry);
                    FileUtils.forceMkdir(new File(root.toString() + File.separator + directoryString));
                    counter++;
                }
            }
        }
        System.out.println("directories created: " + counter);
    }

    private void createThreeTypeCacheDir(File root) throws IOException {
        int counter = 0;
        for (String type_entry : type_entries) {
            for (String first_entry : dir_entries) {
                for (String second_entry : dir_entries) {
                    for (String third_entry : dir_entries) {
                        String directoryString = String.format("%s%s%s", first_entry, second_entry, third_entry);
                        System.out.println(directoryString);
                        FileUtils.forceMkdir(new File(root.toString() + File.separator + type_entry + File.separator + directoryString));
                        counter++;
                    }
                }
            }
        }
        System.out.println("directories created: " + counter);
    }
}
