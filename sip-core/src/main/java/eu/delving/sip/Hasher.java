/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.sip;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This interface describes how files are stored by the sip-creator
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Hasher {
    private static final String SEPARATOR = "__";
    private static final int BLOCK_SIZE = 4096;
    private MessageDigest messageDigest;

    public static String getName(File file) {
        String fileName = file.getName();
        int hashSeparator = fileName.indexOf(SEPARATOR);
        if (hashSeparator > 0) {
            fileName = fileName.substring(hashSeparator + 2);
        }
        return fileName;
    }

    public static String getHash(File file) {
        return getHash(file.getName());
    }

    public static String getHash(String fileName) {
        int hashSeparator = fileName.indexOf(SEPARATOR);
        if (hashSeparator > 0) {
            return fileName.substring(0, hashSeparator);
        }
        else {
            return null;
        }
    }

    public static File hashFile(File file) throws FileStoreException {
        if (file.getName().contains(SEPARATOR)) {
            return file;
        }
        else {
            Hasher hasher = new Hasher();
            hasher.update(file);
            File hashedFile = new File(file.getParentFile(), hasher.getHash() + SEPARATOR + file.getName());
            if (!file.renameTo(hashedFile)) {
                throw new FileStoreException(String.format("Unable to rename %s to %s", file.getAbsolutePath(), hashedFile.getAbsolutePath()));
            }
            return hashedFile;
        }
    }

    public Hasher() {
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available??");
        }
    }

    public void update(byte[] buffer, int bytes) {
        messageDigest.update(buffer, 0, bytes);
    }

    public void update(File inputFile) throws FileStoreException {
        try {
            InputStream inputStream = new FileInputStream(inputFile);
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;
            while (-1 != (bytesRead = inputStream.read(buffer))) {
                update(buffer, bytesRead);
            }
            inputStream.close();
        }
        catch (Exception e) {
            throw new FileStoreException("Unable to get hash of " + inputFile.getAbsolutePath(), e);
        }
    }

    public String getHash() {
        return toHexadecimal(messageDigest.digest());
    }

    public String toString() {
        return getHash();
    }

    static final String HEXES = "0123456789ABCDEF";

    private static String toHexadecimal(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
