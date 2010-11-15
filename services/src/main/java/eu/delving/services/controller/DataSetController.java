package eu.delving.services.controller;

import eu.delving.core.metadata.MetadataException;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.SourceDetails;
import eu.delving.core.rest.DataSetInfo;
import eu.delving.core.rest.ServiceAccessToken;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.BadArgumentException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
public class DataSetController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private MetaRepo metaRepo;

    @Autowired
    private MetadataModel metadataModel;

    @Autowired
    private ServiceAccessToken serviceAccessToken;

    @Autowired
    @Qualifier("solrUpdateServer")
    private SolrServer solrServer;

    @ExceptionHandler(AccessKeyException.class)
    public
    @ResponseBody
    String accessKey(AccessKeyException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        return String.format("<?xml version=\"1.0\">\n<error>\n%s\n</error>\n", e.getMessage());
    }

    @RequestMapping("/administrator/dataset")
    public ModelAndView secureListAll() throws BadArgumentException, AccessKeyException {
        return view(metaRepo.getDataSets());
    }

    @RequestMapping("/dataset")
    public ModelAndView listAll(
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException, AccessKeyException {
        checkAccessKey(accessKey);
        return view(metaRepo.getDataSets());
    }

    @RequestMapping(value = "/administrator/dataset/{dataSetSpec}")
    public ModelAndView secureIndexingControl(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) Boolean enable
    ) throws BadArgumentException, IOException, SolrServerException {
        return indexingControlInternal(dataSetSpec, enable);
    }

    @RequestMapping(value = "/dataset/{dataSetSpec}")
    public ModelAndView indexingControl(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) Boolean enable,
            @RequestParam(required = false) String accessKey
    ) throws BadArgumentException, IOException, SolrServerException, AccessKeyException {
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
                        dataSet.setRecordsIndexed(0);
                        solrServer.deleteByQuery("europeana_collectionName:" + dataSet.getSpec());
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

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/source-details.txt", method = RequestMethod.POST)
    public
    @ResponseBody
    String submitDetails(
            @PathVariable String dataSetSpec,
            InputStream inputStream,
            @RequestParam(required = false) String accessKey
    ) throws AccessKeyException, BadArgumentException, MetadataException {
        checkAccessKey(accessKey);
        log.info("submit details for " + dataSetSpec);
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        SourceDetails details = SourceDetails.read(inputStream);
        if (dataSet == null) {
            dataSet = metaRepo.createDataSet(dataSetSpec);
        }
        dataSet.setName(details.get("name"));
        dataSet.setProviderName(details.get("provider"));
        dataSet.setDescription(details.get("description"));
        dataSet.setRecordRoot(new Path(details.get("recordRoot")));
        dataSet.getMetadataFormat().setPrefix(details.get("prefix"));
        dataSet.getMetadataFormat().setNamespace(details.get("URI"));
        dataSet.getMetadataFormat().setSchema(details.get("schema"));
        dataSet.getMetadataFormat().setAccessKeyRequired(true);
        dataSet.setRecordRoot(new Path(details.get("recordRoot")));
        dataSet.setUniqueElement(new Path(details.get("uniqueElement")));
        dataSet.save();
        return "OK";
    }

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/source.{hash}.xml.gz", method = RequestMethod.POST)
    public
    @ResponseBody
    String submitSource(
            @PathVariable String dataSetSpec,
            @PathVariable String hash,
            InputStream inputStream,
            @RequestParam(required = false) String accessKey
    ) throws AccessKeyException, BadArgumentException, MetadataException, XMLStreamException, IOException {
        checkAccessKey(accessKey);
        log.info("submit source for " + dataSetSpec);
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return String.format("Data set %s not found", dataSetSpec);
        }
        // todo: check the hash??
        dataSet.parseRecords(new GZIPInputStream(inputStream));
        dataSet.save();
        return "OK";
    }

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/mapping.{prefix}", method = RequestMethod.POST)
    public
    @ResponseBody
    String submitMapping(
            @PathVariable String dataSetSpec,
            @PathVariable String prefix,
            InputStream inputStream,
            @RequestParam(required = false) String accessKey
    ) throws AccessKeyException, BadArgumentException, MetadataException, XMLStreamException, IOException {
        checkAccessKey(accessKey);
        log.info("submit mapping for " + dataSetSpec);
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return String.format("Data set %s not found", dataSetSpec);
        }
        RecordMapping mapping = RecordMapping.read(inputStream, metadataModel.getRecordDefinition());
//        String xml = RecordMapping.toXml(mapping);
//        dataSet.addMapping(xml);
        // todo: check the hash??
        dataSet.save();
        return "OK";
    }

    private void checkAccessKey(String accessKey) throws AccessKeyException {
        if (accessKey == null) {
            log.warn("Service Access Key missing");
            throw new AccessKeyException("Access Key missing");
        }
        else if (!serviceAccessToken.checkKey(accessKey)) {
            log.warn(String.format("Service Access Key %s invalid!", accessKey));
            throw new AccessKeyException(String.format("Access Key %s not accepted", accessKey));
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
        info.state = dataSet.getState().toString();
        info.recordsIndexed = dataSet.getRecordsIndexed();
        info.recordCount = dataSet.getRecordCount();
        info.errorMessage = dataSet.getErrorMessage();
        return info;
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
