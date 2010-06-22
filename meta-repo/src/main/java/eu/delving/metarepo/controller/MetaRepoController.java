package eu.delving.metarepo.controller;

import eu.delving.metarepo.core.MetaRepo;
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

/**
 * The controller for the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class MetaRepoController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private MetaRepo metaRepo;

    @RequestMapping("/index.html")
    public
    @ResponseBody
    String list() {
        StringBuilder out = new StringBuilder("<h1>MetaRepo Collections:</h1><ul>\n");
        for (MetaRepo.DataSet name : metaRepo.getDataSets().values()) {
            out.append(String.format("<li><a href=\"%s/index.html\">%s</a></li>", name, name));
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/{dataSetSpec}/index.html")
    public
    @ResponseBody
    String listCollection(
            @PathVariable String dataSetSpec
    ) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSets().get(dataSetSpec);
        StringBuilder out = new StringBuilder(String.format("<h1>MetaRepo Collection %s</h1><ul>\n", dataSet.setSpec()));
        for (MetaRepo.Record record : dataSet.records(0,10)) {
            String xml = record.xml().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");
            out.append("<li>").append(record.identifier()).append("<br>")
                    .append(record.modified().toString()).append("<br>")
                    .append(xml).append("</li>\n");
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/submit/{dataSetSpec}.zip")
    public
    @ResponseBody
    String submit(
            @PathVariable String dataSetSpec,
            InputStream inputStream
    ) throws IOException, XMLStreamException {
        log.info("submit(" + dataSetSpec + ")");
        MetaRepo.DataSet dataSet = metaRepo.getDataSets().get(dataSetSpec);
        if (dataSet == null) {
            dataSet = metaRepo.createDataSet(
                   dataSetSpec, "name", "providerName", "description" // todo: get them from a zip entry!
            );
        }
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("entry: " + entry);
            if (entry.getName().endsWith(".xml")) {
                dataSet.parseRecords(
                        zis,
                        QName.valueOf("{http://www.openarchives.org/OAI/2.0/}record"),
                        QName.valueOf("{http://www.openarchives.org/OAI/2.0/}identifier")
                );
            }
            else if (entry.getName().endsWith(".mapping")) {
                dataSet.setMapping(MetaRepo.DataSet.DATASET_MAPPING_TO_ESE, getMapping(zis));
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
