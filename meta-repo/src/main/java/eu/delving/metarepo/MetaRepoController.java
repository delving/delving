package eu.delving.metarepo;

import com.mongodb.DB;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    public void submit(
            @PathVariable String collectionId,
            InputStream inputStream
    ) throws IOException {
        log.info("submit(" + collectionId + ")");
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("entry: " + entry);
            byte [] buffer = new byte[2048];
            int size;
            while ((size = zis.read(buffer)) != -1) {
                log.info("buffer "+size);
            }
        }
    }

}
