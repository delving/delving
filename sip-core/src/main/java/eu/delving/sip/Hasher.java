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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This interface describes how files are stored by the sip-creator
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Hasher {

    public static final int BLOCK_SIZE = 4096;
    private MessageDigest messageDigest;

    public Hasher() {
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available??");
        }
    }

    public void digest(byte [] buffer, int bytes) {
        try {
            messageDigest.digest(buffer, 0, bytes);
        }
        catch (DigestException e) {
            throw new RuntimeException("Digest config problem", e);
        }
    }

    public void digest(File inputFile) throws FileStoreException {
        try {
            InputStream inputStream = new FileInputStream(inputFile);
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;
            while (-1 != (bytesRead = inputStream.read(buffer))) {
                digest(buffer, bytesRead);
            }
            inputStream.close();
        }
        catch (Exception e) {
            throw new FileStoreException("Unable to get hash of " + inputFile.getAbsolutePath(), e);
        }
    }

    public String toString() {
        return toHexadecimal(messageDigest.digest());
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available??");
        }
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
