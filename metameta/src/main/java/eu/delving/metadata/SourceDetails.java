package eu.delving.metadata;

import com.thoughtworks.xstream.XStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The extra data required when uploading a zip to the repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SourceDetails {
    private Map<String, String> map = new TreeMap<String, String>();
    public static final String RECORD_PATH = "recordPath";
    public static final String UNIQUE_ELEMENT_PATH = "uniqueElementPath";
    public static final String RECORD_COUNT = "recordCount";

    public boolean set(String fieldName, String value) {
        if (!FIELD_SET.contains(fieldName)) {
            throw new IllegalArgumentException(String.format("[%s] is not a source details field", fieldName));
        }
        String existing = map.get(fieldName);
        if (existing == null || !value.equals(existing)) {
            map.put(fieldName, value);
            return true;
        }
        return false;
    }

    public String get(String fieldName) {
        if (!FIELD_SET.contains(fieldName)) {
            throw new IllegalArgumentException(String.format("[%s] is not a source details field", fieldName));
        }
        String value = map.get(fieldName);
        if (value == null) {
            map.put(fieldName, value = "");
        }
        return value;
    }

    private static SourceDetailsDefinition sourceDetailsDefinition;
    private static Set<String> FIELD_SET = new TreeSet<String>();

    static {
        try {
            if (sourceDetailsDefinition == null) {
                XStream stream = new XStream();
                stream.processAnnotations(SourceDetailsDefinition.class);
                sourceDetailsDefinition = (SourceDetailsDefinition) stream.fromXML(SourceDetails.class.getResource("/source-details-definition.xml").openStream());
                for (ConstantInputDefinition constantInputDefinition : sourceDetailsDefinition.constants) {
                    FIELD_SET.add(constantInputDefinition.name);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to read source-details-definition.xml from resources");
        }
    }

    public static SourceDetailsDefinition definition() {
        return sourceDetailsDefinition;
    }

    public static SourceDetails read(InputStream inputStream) throws MetadataException {
        try {
            SourceDetails sourceDetails = new SourceDetails();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) continue;
                int equals = line.indexOf("=");
                if (equals < 0) {
                    continue;
                }
                String fieldName = line.substring(0, equals).trim();
                String value = line.substring(equals+1).trim();
                if (FIELD_SET.contains(fieldName)) {
                    sourceDetails.set(fieldName, value);
                }
            }
            in.close();
            return sourceDetails;
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new MetadataException("Unable to read source details", e);
        }
    }

    public static void write(SourceDetails sourceDetails, OutputStream outputStream) throws MetadataException {
        try {
            Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
            for (ConstantInputDefinition constantInputDefinition : sourceDetailsDefinition.constants) {
                writer.write(String.format("%s=%s\n", constantInputDefinition.name, sourceDetails.get(constantInputDefinition.name)));
            }
            writer.close();
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new MetadataException("Unable to write source details", e);
        }
    }
}
