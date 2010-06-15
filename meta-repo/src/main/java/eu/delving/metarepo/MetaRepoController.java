package eu.delving.metarepo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MetadataRepository metadataRepository;

    @RequestMapping("/index.html")
    public
    @ResponseBody
    String list() {
        StringBuilder out = new StringBuilder("<h1>MetaRepo Collections:</h1><ul>\n");
        for (String name : metadataRepository.getCollectionNames()) {
            out.append(String.format("<li><a href=\"%s/index.html\">%s</a></li>", name, name));
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/{collectionId}/index.html")
    public
    @ResponseBody
    String listCollection(
            @PathVariable String collectionId
    ) {
        MetadataRepository.Collection collection = metadataRepository.getCollection(collectionId);
        StringBuilder out = new StringBuilder(String.format("<h1>MetaRepo Collection %s</h1><ul>\n", collection.name()));
        for (MetadataRepository.Record record : collection.records(0,10)) {
            String xml = record.xml().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");
            out.append("<li>").append(record.identifier()).append("<br>").append(xml).append("</li>\n");
        }
        out.append("</ul>");
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
        MetadataRepository.Collection collection = metadataRepository.getCollection(collectionId);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("entry: " + entry);
            if (entry.getName().endsWith(".xml")) {
                collection.parseRecords(zis, QName.valueOf("{http://www.openarchives.org/OAI/2.0/}record"));
            }
            else if (entry.getName().endsWith(".mapping")) {
                collection.setMapping(MRConstants.FORMAT_ESE, getMapping(zis));
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


    private String getMapping(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        StringBuilder out = new StringBuilder();
        while ((line = in.readLine()) != null) {
            out.append(line).append('\n');
        }
        return out.toString();
    }
}
