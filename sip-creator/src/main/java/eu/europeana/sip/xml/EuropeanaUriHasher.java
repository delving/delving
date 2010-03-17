package eu.europeana.sip.xml;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Store a key to a field for Solr
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EuropeanaUriHasher {
    private static final String RESOLVABLE_URI = "http://www.europeana.eu/resolve/record/";

    public static String createEuropeanaUri(String collectionId, String value) {
        return RESOLVABLE_URI + collectionId + "/" + createHash(value);
    }

    private static String createHash(String uri) {
        byte[] raw;
        synchronized (DIGEST) {
            DIGEST.reset();
            try {
                raw = DIGEST.digest(uri.getBytes("UTF-8"));
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

    private static final MessageDigest DIGEST;

    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
}