package eu.europeana.solrj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Marshal and unmarshal a SolrQuery into a byte array, also able to work with base64 encodings.
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class SolrQueryBlob {
    private byte [] bytes;

    public SolrQueryBlob(SolrQuery solrQuery) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(out));
        objectOutputStream.writeObject(solrQuery);
        objectOutputStream.close();
        this.bytes = out.toByteArray();
    }

    public SolrQueryBlob(byte[] bytes) {
        this.bytes = bytes;
    }


    public SolrQueryBlob(String base64Encoding) throws IOException {
        this.bytes = Base64.decodeBase64(base64Encoding);
    }

    public SolrQuery getQuery() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
        return (SolrQuery)in.readObject();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String toString() {
        return Base64.encodeBase64String(bytes);
    }
}
