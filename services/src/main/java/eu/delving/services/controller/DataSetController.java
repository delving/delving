package eu.delving.services.controller;

import eu.delving.metadata.Facts;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.MetadataNamespace;
import eu.delving.metadata.Path;
import eu.delving.metadata.RecordMapping;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.RecordParseException;
import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetCommand;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetResponse;
import eu.delving.sip.DataSetResponseCode;
import eu.delving.sip.DataSetState;
import eu.delving.sip.FileType;
import eu.delving.sip.Hasher;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
    private AccessKey accessKey;

    @Autowired
    @Qualifier("solrUpdateServer")
    private SolrServer solrServer;

    @RequestMapping("/administrator/dataset")
    public ModelAndView secureListAll() {
        try {
            return view(metaRepo.getDataSets());
        }
        catch (Exception e) {
            return view(e);
        }
    }

    @RequestMapping("/dataset")
    public ModelAndView listAll(
            @RequestParam(required = false) String accessKey
    ) {
        try {
            checkAccessKey(accessKey);
            return view(metaRepo.getDataSets());
        }
        catch (Exception e) {
            return view(e);
        }
    }

    @RequestMapping(value = "/administrator/dataset/{dataSetSpec}/{command}")
    public ModelAndView secureIndexingControl(
            @PathVariable String dataSetSpec,
            @PathVariable String command
    ) {
        return indexingControlInternal(dataSetSpec, command);
    }

    @RequestMapping(value = "/dataset/{dataSetSpec}/{command}")
    public ModelAndView indexingControl(
            @PathVariable String dataSetSpec,
            @PathVariable String command,
            @RequestParam(required = false) String accessKey
    ) {
        try {
            checkAccessKey(accessKey);
            return indexingControlInternal(dataSetSpec, command);
        }
        catch (Exception e) {
            return view(e);
        }
    }

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/{fileType}/{fileName}", method = RequestMethod.GET)
    public ModelAndView checkFile(
            @PathVariable String dataSetSpec,
            @PathVariable String fileType,
            @PathVariable String fileName,
            @RequestParam(required = false) String accessKey
    ) {
        try {
            checkAccessKey(accessKey);
            FileType type = FileType.valueOf(fileType);
            log.info(String.format("check type %s for %s: %s", type, dataSetSpec, fileName));
            String hash = Hasher.getHash(fileName);
            if (hash == null) {
                throw new RuntimeException("No hash available for file name " + fileName);
            }
            DataSetResponseCode response;
            switch (type) {
                case FACTS:
                    response = checkFacts(dataSetSpec, hash);
                    break;
                case SOURCE:
                    response = checkSource(dataSetSpec, hash);
                    break;
                case MAPPING:
                    response = checkMapping(dataSetSpec, hash);
                    break;
                default:
                    response = DataSetResponseCode.SYSTEM_ERROR;
                    break;
            }
            return view(response);
        }
        catch (Exception e) {
            return view(e);
        }
    }

    @RequestMapping(value = "/dataset/submit/{dataSetSpec}/{fileType}/{fileName}", method = RequestMethod.POST)
    public ModelAndView acceptFile(
            @PathVariable String dataSetSpec,
            @PathVariable String fileType,
            @PathVariable String fileName,
            InputStream inputStream,
            @RequestParam(required = false) String accessKey
    ) {
        try {
            checkAccessKey(accessKey);
            FileType type = FileType.valueOf(fileType);
            log.info(String.format("accept type %s for %s: %s", type, dataSetSpec, fileName));
            String hash = Hasher.getHash(fileName);
            if (hash == null) {
                throw new RuntimeException("No hash available for file name " + fileName);
            }
            DataSetResponseCode response;
            switch (type) {
                case FACTS:
                    response = receiveFacts(Facts.read(inputStream), dataSetSpec, hash);
                    break;
                case SOURCE:
                    response = receiveSource(new GZIPInputStream(inputStream), dataSetSpec, hash);
                    break;
                case MAPPING:
                    response = receiveMapping(RecordMapping.read(inputStream, metadataModel), dataSetSpec, hash);
                    break;
                default:
                    response = DataSetResponseCode.SYSTEM_ERROR;
                    break;
            }
            return view(response);
        }
        catch (Exception e) {
            return view(e);
        }
    }

    private DataSetResponseCode receiveMapping(RecordMapping recordMapping, String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        if (dataSet.hasHash(hash)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        dataSet.setMapping(recordMapping);
        dataSet.setMappingHash(recordMapping.getPrefix(), hash);
        dataSet.save();
        return DataSetResponseCode.THANK_YOU;
    }

    private DataSetResponseCode checkMapping(String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        if (dataSet.hasHash(hash)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        else {
            return DataSetResponseCode.READY_TO_RECEIVE;
        }
    }

    private DataSetResponseCode receiveSource(InputStream inputStream, String dataSetSpec, String hash) throws RecordParseException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        if (dataSet.hasHash(hash)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        dataSet.parseRecords(inputStream);
        dataSet.setSourceHash(hash);
        dataSet.save();
        return DataSetResponseCode.THANK_YOU;
    }

    private DataSetResponseCode checkSource(String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        if (dataSet.hasHash(hash)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        else {
            return DataSetResponseCode.READY_TO_RECEIVE;
        }
    }

    private DataSetResponseCode checkFacts(String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            dataSet = metaRepo.createDataSet(dataSetSpec);
        }
        if (dataSet.hasHash(hash)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        else {
            return DataSetResponseCode.READY_TO_RECEIVE;
        }
    }

    private DataSetResponseCode receiveFacts(Facts facts, String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            dataSet = metaRepo.createDataSet(dataSetSpec);
        }
        if (dataSet.hasHash(hash)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        MetaRepo.Details details = dataSet.createDetails();
        details.setName(facts.get("name"));
        details.setProviderName(facts.get("provider"));
        details.setDescription(facts.get("name"));
        String prefix = facts.get("namespacePrefix");
        for (MetadataNamespace metadataNamespace : MetadataNamespace.values()) {
            if (metadataNamespace.getPrefix().equals(prefix)) {
                details.getMetadataFormat().setPrefix(prefix);
                details.getMetadataFormat().setNamespace(metadataNamespace.getUri());
                details.getMetadataFormat().setSchema(metadataNamespace.getSchema());
                details.getMetadataFormat().setAccessKeyRequired(true);
                break;
            }
        }
        details.setRecordRoot(new Path(facts.getRecordRootPath()));
        details.setUniqueElement(new Path(facts.getUniqueElementPath()));
        dataSet.setFactsHash(hash);
        dataSet.save();
        return DataSetResponseCode.THANK_YOU;
    }

    private void checkAccessKey(String accessKey) throws AccessKeyException {
        if (accessKey == null) {
            log.warn("Service Access Key missing");
            throw new AccessKeyException("Access Key missing");
        }
        else if (!this.accessKey.checkKey(accessKey)) {
            log.warn(String.format("Service Access Key %s invalid!", accessKey));
            throw new AccessKeyException(String.format("Access Key %s not accepted", accessKey));
        }
    }

    private ModelAndView indexingControlInternal(String dataSetSpec, String commandString) {
        try {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
            if (dataSet == null) {
                throw new DataSetNotFoundException(String.format("String %s does not exist", dataSetSpec));
            }
            DataSetCommand command = DataSetCommand.valueOf(commandString);
            switch (command) {
                case DISABLE:
                    switch (dataSet.getState()) {
                        case QUEUED:
                        case INDEXING:
                        case ERROR:
                        case ENABLED:
                            dataSet.setState(DataSetState.DISABLED);
                            dataSet.setRecordsIndexed(0);
                            dataSet.save();
                            deleteFromSolr(dataSet);
                            return view(dataSet);
                        default :
                            return view(DataSetResponseCode.STATE_CHANGE_FAILURE);
                    }
                case INDEX:
                    switch (dataSet.getState()) {
                        case EMPTY: // todo: make sure the data set goes to upload
                        case DISABLED:
                        case UPLOADED:
                            dataSet.setState(DataSetState.QUEUED);
                            dataSet.save();
                            return view(dataSet);
                        default :
                            return view(DataSetResponseCode.STATE_CHANGE_FAILURE);
                    }
                case REINDEX:
                    switch (dataSet.getState()) {
                        case ENABLED:
                            dataSet.setRecordsIndexed(0);
                            dataSet.setState(DataSetState.QUEUED);
                            dataSet.save();
                            return view(dataSet);
                        default :
                            return view(DataSetResponseCode.STATE_CHANGE_FAILURE);
                    }
                default:
                    throw new RuntimeException();
            }
        }
        catch (Exception e) {
            return view(e);
        }
    }

    private void deleteFromSolr(MetaRepo.DataSet dataSet) throws SolrServerException, IOException {
        solrServer.deleteByQuery("europeana_collectionName:" + dataSet.getSpec());
    }

    private ModelAndView view(DataSetResponseCode responseCode) {
        return view(new DataSetResponse(responseCode));
    }

    private ModelAndView view(Exception exception) {
        log.warn("Problem in controller", exception);
        DataSetResponseCode code;
        if (exception instanceof AccessKeyException) {
            code = DataSetResponseCode.ACCESS_KEY_FAILURE;
        }
        else if (exception instanceof DataSetNotFoundException) {
            code = DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        else {
            code = DataSetResponseCode.SYSTEM_ERROR;
        }
        return view(new DataSetResponse(code));
    }

    private ModelAndView view(MetaRepo.DataSet dataSet) throws DataSetNotFoundException {
        if (dataSet == null) {
            throw new DataSetNotFoundException("Data Set was null");
        }
        DataSetResponse response = new DataSetResponse(DataSetResponseCode.THANK_YOU);
        response.addDataSetInfo(getInfo(dataSet));
        return new ModelAndView("dataSetXmlView", BindingResult.MODEL_KEY_PREFIX + "response", response);
    }

    private ModelAndView view(Collection<? extends MetaRepo.DataSet> dataSetList) {
        DataSetResponse response = new DataSetResponse(DataSetResponseCode.THANK_YOU);
        for (MetaRepo.DataSet dataSet : dataSetList) {
            response.addDataSetInfo(getInfo(dataSet));
        }
        return view(response);
    }

    private ModelAndView view(DataSetResponse response) {
        return new ModelAndView("dataSetXmlView", BindingResult.MODEL_KEY_PREFIX + "response", response);
    }

    private DataSetInfo getInfo(MetaRepo.DataSet dataSet) {
        DataSetInfo info = new DataSetInfo();
        info.spec = dataSet.getSpec();
        info.state = dataSet.getState().toString();
        info.recordCount = dataSet.getRecordCount();
        info.errorMessage = dataSet.getErrorMessage();
        info.recordsIndexed = dataSet.getRecordsIndexed();
        info.name = dataSet.getDetails().getName();
        return info;
    }
}
