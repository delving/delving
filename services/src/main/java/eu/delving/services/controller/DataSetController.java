/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.services.controller;

import eu.delving.metadata.Facts;
import eu.delving.metadata.Hasher;
import eu.delving.metadata.MetadataException;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.MetadataNamespace;
import eu.delving.metadata.RecordMapping;
import eu.delving.metadata.SourceStream;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.MappingNotFoundException;
import eu.delving.services.exceptions.RecordParseException;
import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetCommand;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetResponse;
import eu.delving.sip.DataSetResponseCode;
import eu.delving.sip.DataSetState;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileType;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private static final int RECORD_STREAM_CHUNK = 1000;
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
            String hash = Hasher.extractHashFromFileName(fileName);
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

    @RequestMapping(value = "/dataset/fetch/{dataSetSpec}-sip.zip", method = RequestMethod.GET)
    public void fetchSIP(
            @PathVariable String dataSetSpec,
            @RequestParam(required = false) String accessKey,
            HttpServletResponse response
    ) {
        try {
            checkAccessKey(accessKey);
            log.info(String.format("requested %s-sip.zip", dataSetSpec));
            response.setContentType("application/zip");
            writeSipZip(dataSetSpec, response.getOutputStream(), accessKey);
            response.setStatus(HttpStatus.OK.value());
            log.info(String.format("returned %s-sip.zip", dataSetSpec));
        }
        catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            log.warn("Problem building sip.zip", e);
        }
    }

    private void writeSipZip(String dataSetSpec, OutputStream outputStream, String accessKey) throws IOException, MappingNotFoundException, AccessKeyException, XMLStreamException, MetadataException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            throw new IOException("Data Set not found"); // IOException?
        }
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        zos.putNextEntry(new ZipEntry(FileStore.FACTS_FILE_NAME));
        Facts facts = Facts.fromBytes(dataSet.getDetails().getFacts());
        facts.setDownloadedSource(true);
        zos.write(Facts.toBytes(facts));
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry(FileStore.SOURCE_FILE_NAME));
        String sourceHash = writeSourceStream(dataSet, zos, accessKey);
        zos.closeEntry();
        for (MetaRepo.Mapping mapping : dataSet.mappings().values()) {
            RecordMapping recordMapping = mapping.getRecordMapping();
            zos.putNextEntry(new ZipEntry(String.format(FileStore.MAPPING_FILE_PATTERN, recordMapping.getPrefix())));
            RecordMapping.write(recordMapping, zos);
            zos.closeEntry();
        }
        zos.finish();
        zos.close();
        dataSet.setSourceHash(sourceHash, true);
        dataSet.save();
    }

    private String writeSourceStream(MetaRepo.DataSet dataSet, ZipOutputStream zos, String accessKey) throws MappingNotFoundException, AccessKeyException, XMLStreamException, IOException {
        SourceStream sourceStream = new SourceStream(zos);
        sourceStream.startZipStream(dataSet.getNamespaces().toMap());
        ObjectId afterId = null;
        while (true) {
            MetaRepo.DataSet.RecordFetch fetch = dataSet.getRecords(
                    dataSet.getDetails().getMetadataFormat().getPrefix(),
                    RECORD_STREAM_CHUNK, null, afterId, null, accessKey
            );
            if (fetch == null) {
                break;
            }
            afterId = fetch.getAfterId();
            for (MetaRepo.Record record : fetch.getRecords()) {
                sourceStream.addRecord(record.getXmlString());
            }
        }
        return sourceStream.endZipStream();
    }

    private DataSetResponseCode receiveMapping(RecordMapping recordMapping, String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        if (hasHash(hash, dataSet)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        dataSet.setMapping(recordMapping, true);
        dataSet.setMappingHash(recordMapping.getPrefix(), hash);
        dataSet.save();
        return DataSetResponseCode.THANK_YOU;
    }

    private DataSetResponseCode receiveSource(InputStream inputStream, String dataSetSpec, String hash) throws RecordParseException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            return DataSetResponseCode.DATA_SET_NOT_FOUND;
        }
        if (hasHash(hash, dataSet)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        dataSet.parseRecords(inputStream);
        dataSet.setSourceHash(hash, false);
        dataSet.save();
        return DataSetResponseCode.THANK_YOU;
    }

    private DataSetResponseCode receiveFacts(Facts facts, String dataSetSpec, String hash) {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(dataSetSpec);
        if (dataSet == null) {
            dataSet = metaRepo.createDataSet(dataSetSpec);
        }
        if (hasHash(hash, dataSet)) {
            return DataSetResponseCode.GOT_IT_ALREADY;
        }
        MetaRepo.Details details = dataSet.createDetails();
        details.setName(facts.get("name"));
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
        dataSet.setFactsHash(hash);
        try {
            details.setFacts(Facts.toBytes(facts));
        }
        catch (MetadataException e) {
            return DataSetResponseCode.SYSTEM_ERROR;
        }
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
            DataSetState state = dataSet.getState(false);
            switch (command) {
                case DISABLE:
                    switch (state) {
                        case QUEUED:
                        case INDEXING:
                        case ERROR:
                        case ENABLED:
                            dataSet.setState(DataSetState.DISABLED);
                            dataSet.setRecordsIndexed(0);
                            dataSet.save();
                            deleteFromSolr(dataSet);
                            return view(dataSet);
                        default:
                            return view(DataSetResponseCode.STATE_CHANGE_FAILURE);
                    }
                case INDEX:
                    switch (state) {
                        case DISABLED:
                        case UPLOADED:
                            dataSet.setState(DataSetState.QUEUED);
                            dataSet.save();
                            return view(dataSet);
                        default:
                            return view(DataSetResponseCode.STATE_CHANGE_FAILURE);
                    }
                case REINDEX:
                    switch (state) {
                        case ENABLED:
                            dataSet.setRecordsIndexed(0);
                            dataSet.setState(DataSetState.QUEUED);
                            dataSet.save();
                            return view(dataSet);
                        default:
                            return view(DataSetResponseCode.STATE_CHANGE_FAILURE);
                    }
                case DELETE:
                    switch (state) {
                        case INCOMPLETE:
                        case DISABLED:
                        case ERROR:
                        case UPLOADED:
                            dataSet.delete();
                            dataSet.setState(DataSetState.INCOMPLETE);
                            return view(dataSet);
                        default:
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
        solrServer.commit();
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

    private boolean hasHash(String hash, MetaRepo.DataSet dataSet) {
        for (String ours : dataSet.getHashes()) {
            if (ours.equals(hash)) {
                return true;
            }
        }
        return false;
    }

    private DataSetInfo getInfo(MetaRepo.DataSet dataSet) {
        DataSetInfo info = new DataSetInfo();
        info.spec = dataSet.getSpec();
        info.name = dataSet.getDetails().getName();
        info.state = dataSet.getState(false).toString();
        info.recordCount = dataSet.getRecordCount();
        info.errorMessage = dataSet.getErrorMessage();
        info.recordsIndexed = dataSet.getRecordsIndexed();
        info.hashes = dataSet.getHashes();
        return info;
    }
}
