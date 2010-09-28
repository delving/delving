package eu.delving.services.controller;

import com.thoughtworks.xstream.XStream;
import eu.delving.core.rest.DataSetInfo;
import eu.delving.core.rest.ServiceAccessToken;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.europeana.sip.core.DataSetDetails;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provide a REST interface for managing datasets.
 *
 * - API Key authentication
 * - list all collections (some details: size, indexing status, available formats)
 * - for each collection
 *          - enable/disable for indexing
 *          - abort indexing
 *          - enable/disable for harvesting
 *          - enable/disable per metadata format
 *          - full statistics
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/dataset")
public class DatasetController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private MetaRepo metaRepo;

    @Autowired
    private ServiceAccessToken serviceAccessToken;

    @RequestMapping
    public ModelAndView listAll(
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException {
        checkAccessKey(accessKey);
        return view(metaRepo.getDataSets());
    }

    @RequestMapping(value = "/indexing/{dataSetSpec}")
    public ModelAndView indexingControl(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) Boolean enable,
            @RequestParam(required = false) Boolean abort,
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException {
        checkAccessKey(accessKey);
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (abort != null && abort) {
            log.info(String.format("Indexing of %s to be aborted", dataSetSpec));
        }
        else if (enable != null) {
            log.info(String.format("Indexing of %s to be %s", dataSetSpec, enable ? "enabled" : "disabled"));
        }
        else {
            log.info(String.format("Just showing %s", dataSetSpec));
        }
        return view(dataSet);
    }

    @RequestMapping(value = "/harvesting/{dataSetSpec}")
    public ModelAndView harvestingControl(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) Boolean enable,
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException {
        checkAccessKey(accessKey);
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (enable != null) {
            if (prefix != null) {
                log.info(String.format("Harvesting of %s prefix %s to be %s", dataSetSpec, prefix, enable ? "enabled" : "disabled"));
            }
            else {
                log.info(String.format("Harvesting of %s to be %s", dataSetSpec, enable ? "enabled" : "disabled"));
            }
        }
        else {
            log.info(String.format("Just showing %s", dataSetSpec));
        }
        return view(dataSet);
    }

    @RequestMapping(value = "/submit/{dataSetSpec}.zip", method= RequestMethod.POST)
    public @ResponseBody
    String submit(
            @PathVariable String dataSetSpec,
            InputStream inputStream,
            @RequestParam(required = false) String accessKey
    ) throws IOException, XMLStreamException, BadArgumentException {
        checkAccessKey(accessKey);
        log.info("submit(" + dataSetSpec + ")");
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        DataSetDetails details = null;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("entry: " + entry);
            if (entry.getName().endsWith(".details")) {
                details = getDetails(zis);
                if (!details.getSpec().equals(dataSetSpec)) {
                    throw new IOException(String.format("Zip file [%s] should have the data spec name [%s]", dataSetSpec, details.getSpec()));
                }
                if (dataSet == null) {
                    dataSet = metaRepo.createDataSet(
                            dataSetSpec,
                            details.getName(),
                            details.getProviderName(),
                            details.getDescription(),
                            details.getPrefix(),
                            details.getNamespace(),
                            details.getSchema()
                    );
                }
                else {
                    dataSet.setName(details.getName());
                    dataSet.setProviderName(details.getProviderName());
                    dataSet.setDescription(details.getDescription());
                    dataSet.setRecordRoot(QName.valueOf(details.getRecordRoot()));
                    dataSet.metadataFormat().setPrefix(details.getPrefix());
                    dataSet.metadataFormat().setNamespace(details.getNamespace());
                    dataSet.metadataFormat().setSchema(details.getSchema());
                    dataSet.save();
                }
            }
            else if (entry.getName().endsWith(".xml")) {
                if (dataSet == null || details == null) {
                    zis.close();
                    throw new IOException("Data set details must come first in the uploaded zip file");
                }
                dataSet.parseRecords(
                        zis,
                        QName.valueOf(details.getRecordRoot()),
                        QName.valueOf(details.getUniqueElement())
                );
            }
            else if (entry.getName().endsWith(".mapping")) {
                if (dataSet == null) {
                    zis.close();
                    throw new IOException("Data set details must come first in the uploaded zip file");
                }
                dataSet.addMapping(getMapping(zis));
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

    private void checkAccessKey(String accessKey) {
        if (accessKey == null) {
            log.warn("!!! Service Access Key missing!");
            // todo: really fail
        }
        else if (!serviceAccessToken.checkKey(accessKey)) {
            log.warn(String.format("!!! Service Access Key %s invalid!", accessKey));
            // todo: really fail
        }
    }

    private ModelAndView view(MetaRepo.DataSet dataSet) {
        if (dataSet == null) {
            throw new RuntimeException("DataSet not found!"); // todo: better solution
        }
        return new ModelAndView("dataSetXmlView", BindingResult.MODEL_KEY_PREFIX + "dataset", getInfo(dataSet));
    }

    private ModelAndView view(Collection<? extends MetaRepo.DataSet> dataSetList) {
        List<DataSetInfo> list = new ArrayList<DataSetInfo>();
        for (MetaRepo.DataSet dataSet : dataSetList) {
            list.add(getInfo(dataSet));
        }
        return new ModelAndView("dataSetXmlView", BindingResult.MODEL_KEY_PREFIX + "list", list);
    }

    private DataSetInfo getInfo(MetaRepo.DataSet dataSet) {
        DataSetInfo info = new DataSetInfo();
        info.spec = dataSet.setSpec();
        info.name = dataSet.setName();
        info.providerName = dataSet.providerName();
        info.prefix = dataSet.metadataFormat().prefix();
        return info;
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
