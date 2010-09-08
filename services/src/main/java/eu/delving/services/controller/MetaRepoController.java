package eu.delving.services.controller;

import com.thoughtworks.xstream.XStream;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.CannotDisseminateFormatException;
import eu.europeana.sip.core.DataSetDetails;
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
import java.util.Map;
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
    String list() throws BadArgumentException {
        StringBuilder out = new StringBuilder("<h1>MetaRepo Collections:</h1><ul>\n");
        for (MetaRepo.DataSet dataSet : metaRepo.getDataSets().values()) {
            out.append(String.format(
                    "<li><a href=\"%s/abm.html\">abm:%s</a></li>",
                    dataSet.setSpec(), dataSet.setSpec()
            ));
            out.append(String.format(
                    "<li><a href=\"%s/ese.html\">ese:%s</a></li>",
                    dataSet.setSpec(), dataSet.setSpec()
            ));
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/formats.html")
    public
    @ResponseBody
    String formats() throws BadArgumentException {
        StringBuilder out = new StringBuilder("<h1>MetaRepo Formats:</h1><ul>\n");
        for (MetaRepo.MetadataFormat format : metaRepo.getMetadataFormats()) {
            out.append(String.format("<li>%s : %s - %s</li>", format.prefix(), format.namespace(), format.schema()));
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/{dataSetSpec}/{prefix}.html")
    public
    @ResponseBody
    String listCollection(
            @PathVariable String dataSetSpec,
            @PathVariable String prefix
    ) throws CannotDisseminateFormatException, BadArgumentException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSets().get(dataSetSpec);
        if (dataSet == null) {
            throw new RuntimeException(String.format("Dataset [%s] not found", dataSetSpec));
        }
        StringBuilder out = new StringBuilder(String.format("<h1>MetaRepo Collection %s in %s format</h1><ul>\n", dataSet.setSpec(), prefix));
        for (MetaRepo.Record record : dataSet.records(prefix, 0, 10, null, null)) {
            String xml = record.xml(prefix).replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");
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
    ) throws IOException, XMLStreamException, BadArgumentException {
        log.info("submit(" + dataSetSpec + ")");
        Map<String, ? extends MetaRepo.DataSet> dataSets = metaRepo.getDataSets();
        MetaRepo.DataSet dataSet = dataSets.get(dataSetSpec);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        DataSetDetails dataSetDetails = null;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("entry: " + entry);
            if (entry.getName().endsWith(".details")) {
                dataSetDetails = getDetails(zis);
                if (!dataSetDetails.getSpec().equals(dataSetSpec)) {
                    throw new IOException(String.format("Zip file [%s] should have the data spec name [%s]", dataSetSpec, dataSetDetails.getSpec()));
                }
                if (dataSet == null) {
                    dataSet = metaRepo.createDataSet(
                            dataSetSpec,
                            dataSetDetails.getName(),
                            dataSetDetails.getProviderName(),
                            dataSetDetails.getDescription(),
                            "abm", // todo: this should be passed in??
                            dataSetDetails.getNamespace(),
                            dataSetDetails.getSchema()
                    );
                }
                else {
                    // todo: update it!
                }
            }
            else if (entry.getName().endsWith(".xml")) {
                if (dataSet == null || dataSetDetails == null) {
                    zis.close();
                    throw new IOException("Data set details must come first in the uploaded zip file");
                }
                dataSet.parseRecords(
                        zis,
                        QName.valueOf(dataSetDetails.getRecordRoot()),
                        QName.valueOf(dataSetDetails.getUniqueElement())
                );
            }
            else if (entry.getName().endsWith(".mapping")) {
                if (dataSet == null) {
                    zis.close();
                    throw new IOException("Data set details must come first in the uploaded zip file");
                }
                dataSet.setMapping(
                        getMapping(zis),
                        "ese", // todo: get this info from the mapping somehow
                        "http://www.europeana.eu/schemas/ese/",
                        "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd"
                );
            }
            else {
                byte[] buffer = new byte[2048];
                int size;
                while ((size = zis.read(buffer)) != -1) {
                    log.warn("SKIPPING " + size);
                }
            }
        }
        zis.close();
        log.info("finished submit");
        return "OK";
    }

    private DataSetDetails getDetails(InputStream inputStream) {
        XStream stream = new XStream();
        stream.processAnnotations(DataSetDetails.class);
        return (DataSetDetails) stream.fromXML(inputStream);
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
