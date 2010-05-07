package eu.europeana.sip.groovy

import java.security.MessageDigest

/**
 * Store a key to a field for Solr
 * Replaces URIHasher.java
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
@EuropeanaExtractable
class EuropeanaUriHasher {

	private static final String RESOLVABLE_URI = "http://www.europeana.eu/resolve/record/";
	private static final MessageDigest DIGEST;

	private static final byte[] HEX_CHAR_TABLE = [
			(byte) '0', (byte) '1', (byte) '2', (byte) '3',
			(byte) '4', (byte) '5', (byte) '6', (byte) '7',
			(byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'
	];

	static {
		try {
			DIGEST = MessageDigest.getInstance("SHA-1");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	String createEuropeanaUri(String collectionId, String value) {
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
		for (byte b: raw) {
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
}