package eu.delving.metarepo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static eu.delving.metarepo.MRConstants.*;

/**
 * The controller for the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class MetaRepoController {

    private Logger log = Logger.getLogger(getClass());

    @Value("#{metarepo}")
    private DB metaRepo;

    @RequestMapping("index.html")
    public
    @ResponseBody
    String list() {
        StringBuilder out = new StringBuilder("MetaRepo Collections:\n");
        for (String name : metaRepo.getCollectionNames()) {
            out.append(name).append('\n');
        }
        return out.toString();
    }

    @RequestMapping("/submit/{collectionId}.zip")
    public
    @ResponseBody
    String submit(
            @PathVariable String collectionId,
            InputStream inputStream
    ) throws IOException, XMLStreamException {
        log.info("submit(" + collectionId + ")");
        DBCollection collection = metaRepo.getCollection(collectionId);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("entry: " + entry);
            if (entry.getName().endsWith(".xml")) {
                parseMetadata(zis, collection);
            }
            else if (entry.getName().endsWith(".mapping")) {
                saveMapping(zis, collection);
            }
            else {
                byte[] buffer = new byte[2048];
                int size;
                while ((size = zis.read(buffer)) != -1) {
                    log.info("buffer " + size);
                }
            }
        }
        zis.close();
        log.info("finished submit");
        return "OK";
    }


    private void parseMetadata(InputStream inputStream, DBCollection collection) throws XMLStreamException, IOException {
        DBObjectParser parser = new DBObjectParser(
                inputStream,
                QName.valueOf("{http://www.openarchives.org/OAI/2.0/}record"), // todo: get this from the zip file
                "ORIG" // todo: what is the metadata format?
        );
        DBObject object;
        while ((object = parser.nextRecord()) != null) {
            collection.insert(object);
        }
    }

    private void saveMapping(InputStream inputStream, DBCollection collection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        StringBuilder out = new StringBuilder();
        while ((line = in.readLine()) != null) {
            out.append(line).append('\n');
        }
        DBObject object = new BasicDBObject();
        object.put(TYPE_ATTR, TYPE_MAPPING);
        object.put(FORMAT_MAPPING, out.toString());
        collection.insert(object);
    }
}
