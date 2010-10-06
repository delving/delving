package eu.delving.services.controller;

import com.thoughtworks.xstream.XStream;
import eu.delving.core.rest.DataSetInfo;
import eu.delving.core.rest.ServiceAccessToken;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.europeana.sip.core.DataSetDetails;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
 * <p/>
 * - API Key authentication
 * - list all collections (some details: size, indexing status, available formats)
 * - for each collection
 * - enable/disable for indexing
 * - abort indexing
 * - enable/disable for harvesting
 * - enable/disable per metadata format
 * - full statistics
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

    @Autowired
    @Qualifier("solrUpdateServer")
    private SolrServer solrServer;

    @ExceptionHandler(BadArgumentException.class)
    public @ResponseBody String exception(BadArgumentException e, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return String.format("<?xml version=\"1.0\">\n<error>\n%s\n</error>\n", e.getMessage());
    }

    @RequestMapping
    public ModelAndView listAll(
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException {
        checkAccessKey(accessKey);
        return view(metaRepo.getDataSets());
    }

    @RequestMapping(value = "/ajaxindexing/{dataSetSpec}")
    public ModelAndView indexingControlForAjax(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) Boolean enable
    ) throws BadArgumentException, IOException, SolrServerException {
        return indexingControlInternal(dataSetSpec, enable);
    }

    @RequestMapping(value = "/indexing/{dataSetSpec}")
    public ModelAndView indexingControl(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) Boolean enable,
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException, IOException, SolrServerException {
        checkAccessKey(accessKey);
        return indexingControlInternal(dataSetSpec, enable);
    }

    private ModelAndView indexingControlInternal(String dataSetSpec, Boolean enable) throws BadArgumentException, SolrServerException, IOException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            throw new BadArgumentException(String.format("String %s does not exist", dataSetSpec));
        }
        if (enable != null) {
            MetaRepo.DataSetState oldState = dataSet.getState();
            switch (dataSet.getState()) {
                case INDEXING:
                case ENABLED:
                case QUEUED:
                    if (!enable) {
                            dataSet.setState(MetaRepo.DataSetState.DISABLED);
                            solrServer.deleteByQuery(dataSet.getSpec());
                    }
                    break;
                case UPLOADED:
                case ERROR:
                case DISABLED:
                    if (enable) {
                        dataSet.setState(MetaRepo.DataSetState.QUEUED);
                    }
                    break;
            }
            if (oldState != dataSet.getState()) {
                dataSet.save();
                log.info(String.format("State of %s changed from %s to %s", dataSetSpec, oldState, dataSet.getState()));
            }
            else {
                log.info(String.format("State of %s unchanged at %s", dataSetSpec, dataSet.getState()));
            }
        }
        else {
            log.info(String.format("Just showing %s", dataSetSpec));
        }
        return view(dataSet);
    }

    @RequestMapping(value = "/submit/{dataSetSpec}.zip", method = RequestMethod.POST)
    public @ResponseBody String submit(
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
                    dataSet.getMetadataFormat().setPrefix(details.getPrefix());
                    dataSet.getMetadataFormat().setNamespace(details.getNamespace());
                    dataSet.getMetadataFormat().setSchema(details.getSchema());
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

    private void checkAccessKey(String accessKey) throws BadArgumentException {
        if (accessKey == null) {
            log.warn("!!! Service Access Key missing!");
            throw new BadArgumentException("Access Key missing");
        }
        else if (!serviceAccessToken.checkKey(accessKey)) {
            log.warn(String.format("!!! Service Access Key %s invalid!", accessKey));
            throw new BadArgumentException(String.format("Access Key %s not accepted", accessKey));
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
        info.spec = dataSet.getSpec();
        info.name = dataSet.getName();
        info.providerName = dataSet.getProviderName();
        info.prefix = dataSet.getMetadataFormat().getPrefix();
        info.state = dataSet.getState().toString();
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
