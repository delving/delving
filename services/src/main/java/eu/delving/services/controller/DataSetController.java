package eu.delving.services.controller;

import eu.delving.metadata.Facts;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.Path;
import eu.delving.metadata.RecordMapping;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetResponse;
import eu.delving.sip.FileType;
import eu.delving.sip.Hasher;
import eu.delving.sip.ServiceAccessToken;
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
import java.io.IOException;
import java.io.InputStream;
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

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/{fileType}/{fileName}", method = RequestMethod.GET)
    public
    @ResponseBody
    String checkFile(
            @PathVariable String dataSetSpec,
            @PathVariable String fileType,
            @PathVariable String fileName,
            @RequestParam(required = false) String accessKey
    ) {
        try {
            checkAccessKey(accessKey);
        }
        catch (AccessKeyException e) {
            return DataSetResponse.ACCESS_KEY_FAILURE.toString();
        }
        try {
            FileType type = FileType.valueOf(fileType);
            log.info(String.format("submit type %s for %s: %s", type, dataSetSpec, fileName));
            String hash = Hasher.getHash(fileName);
            if (hash == null) {
                throw new RuntimeException("No hash available for file name " + fileName);
            }
            switch (type) {
                case FACTS:
                    return checkFacts(dataSetSpec, hash).toString();
                case SOURCE:
                    return checkSource(dataSetSpec, hash).toString();
                case MAPPING:
                    return checkMapping(dataSetSpec, hash).toString();
                default:
                    return DataSetResponse.SYSTEM_ERROR.toString();
            }
        }
        catch (Exception e) {
            log.error("Unable to submit", e);
        }
        return DataSetResponse.SYSTEM_ERROR.toString();
    }

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/{fileType}/{fileName}", method = RequestMethod.POST)
    public
    @ResponseBody
    DataSetResponse submitFile(
            @PathVariable String dataSetSpec,
            @PathVariable String fileType,
            @PathVariable String fileName,
            InputStream inputStream,
            @RequestParam(required = false) String accessKey
    ) {
        try {
            checkAccessKey(accessKey);
        }
        catch (AccessKeyException e) {
            return DataSetResponse.ACCESS_KEY_FAILURE;
        }
        try {
            FileType type = FileType.valueOf(fileType);
            log.info(String.format("submit type %s for %s: %s", type, dataSetSpec, fileName));
            String hash = Hasher.getHash(fileName);
            if (hash == null) {
                throw new RuntimeException("No hash available for file name " + fileName);
            }
            switch (type) {
                case FACTS:
                    return receiveFacts(Facts.read(inputStream), dataSetSpec, hash);
                case SOURCE:
                    return receiveSource(new GZIPInputStream(inputStream), dataSetSpec, hash);
                case MAPPING:
                    return receiveMapping(RecordMapping.read(inputStream, metadataModel), dataSetSpec, hash);
                default:
                    return DataSetResponse.SYSTEM_ERROR;
            }
        }
        catch (Exception e) {
            log.error("Unable to submit", e);
        }
        return DataSetResponse.SYSTEM_ERROR;
    }

    private DataSetResponse receiveMapping(RecordMapping recordMapping, String dataSetSpec, String hash) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                return DataSetResponse.DATA_SET_NOT_FOUND;
            }
            if (dataSet.hasHash(hash)) {
                return DataSetResponse.GOT_IT_ALREADY;
            }
            dataSet.setMapping(recordMapping);
            dataSet.save();
            return DataSetResponse.THANK_YOU;
        }
        catch (BadArgumentException e) {
            log.error("Unable to receive mapping", e);
            return DataSetResponse.SYSTEM_ERROR;
        }
    }

    private DataSetResponse checkMapping(String dataSetSpec, String hash) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                return DataSetResponse.DATA_SET_NOT_FOUND;
            }
            if (dataSet.hasHash(hash)) {
                return DataSetResponse.GOT_IT_ALREADY;
            }
            else {
                return DataSetResponse.READY_TO_RECEIVE;
            }
        }
        catch (BadArgumentException e) {
            log.error("Unable to check mapping", e);
            return DataSetResponse.SYSTEM_ERROR;
        }
    }

    private DataSetResponse receiveSource(InputStream inputStream, String dataSetSpec, String hash) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                return DataSetResponse.DATA_SET_NOT_FOUND;
            }
            if (dataSet.hasHash(hash)) {
                return DataSetResponse.GOT_IT_ALREADY;
            }
            dataSet.parseRecords(new GZIPInputStream(inputStream));
            dataSet.save();
            return DataSetResponse.THANK_YOU;
        }
        catch (Exception e) {
            log.error("Unable to receive source", e);
            return DataSetResponse.SYSTEM_ERROR;
        }
    }

    private DataSetResponse checkSource(String dataSetSpec, String hash) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                return DataSetResponse.DATA_SET_NOT_FOUND;
            }
            if (dataSet.hasHash(hash)) {
                return DataSetResponse.GOT_IT_ALREADY;
            }
            else {
                return DataSetResponse.READY_TO_RECEIVE;
            }
        }
        catch (Exception e) {
            log.error("Unable to check source", e);
            return DataSetResponse.SYSTEM_ERROR;
        }
    }

    private DataSetResponse checkFacts(String dataSetSpec, String hash) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                return DataSetResponse.READY_TO_RECEIVE;
            }
            if (dataSet.hasHash(hash)) {
                return DataSetResponse.GOT_IT_ALREADY;
            }
            else {
                return DataSetResponse.READY_TO_RECEIVE;
            }
        }
        catch (BadArgumentException e) {
            log.error("Unable to check facts", e);
            return DataSetResponse.SYSTEM_ERROR;
        }
    }

    private DataSetResponse receiveFacts(Facts facts, String dataSetSpec, String hash) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                dataSet = metaRepo.createDataSet(dataSetSpec);
            }
            if (dataSet.hasHash(hash)) {
                return DataSetResponse.GOT_IT_ALREADY;
            }
            dataSet.setName(facts.get("name"));
            dataSet.setProviderName(facts.get("provider"));
            dataSet.setDescription(facts.get("description"));
            dataSet.getMetadataFormat().setPrefix(facts.get("prefix"));
            dataSet.getMetadataFormat().setNamespace(facts.get("URI"));
            dataSet.getMetadataFormat().setSchema(facts.get("schema"));
            dataSet.getMetadataFormat().setAccessKeyRequired(true);
            dataSet.setRecordRoot(new Path(facts.get("recordRoot")));
            dataSet.setUniqueElement(new Path(facts.get("uniqueElement")));
            dataSet.setFactsHash(hash);
            dataSet.save();
            return DataSetResponse.THANK_YOU;
        }
        catch (BadArgumentException e) {
            log.error("Unable to receive facts", e);
            return DataSetResponse.SYSTEM_ERROR;
        }
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
}
