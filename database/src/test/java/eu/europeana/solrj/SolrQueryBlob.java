package eu.europeana.solrj;

import org.apache.solr.client.solrj.SolrQuery;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
        BASE64Decoder decoder = new BASE64Decoder();
        this.bytes = decoder.decodeBuffer(base64Encoding);
    }

    public SolrQuery getQuery() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
        return (SolrQuery)in.readObject();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String toString() {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes);
    }
}
