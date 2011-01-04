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

package eu.delving.services.core.impl;

import com.mongodb.DBObject;
import eu.delving.metadata.MetadataException;
import eu.delving.metadata.RecordMapping;
import eu.delving.metadata.RecordValidator;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.MappingNotFoundException;
import eu.delving.services.exceptions.MetaRepoSystemException;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementing the mapping interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class MappingImpl implements MetaRepo.Mapping, MappingInternal, Comparable<MetaRepo.Mapping> {
    private Logger log = Logger.getLogger(getClass());
    private ImplFactory implFactory;
    private MetaRepo.DataSet dataSet;
    private DBObject object;
    private MetaRepo.MetadataFormat metadataFormat;
    private RecordValidator recordValidator;
    private MappingRunner mappingRunner;

    MappingImpl(ImplFactory implFactory, MetaRepo.DataSet dataSet, DBObject object) {
        this.implFactory = implFactory;
        this.dataSet = dataSet;
        this.object = object;
        this.metadataFormat = new MetadataFormatImpl((DBObject) object.get(FORMAT));
        this.recordValidator = new RecordValidator(implFactory.getMetadataModel().getRecordDefinition(metadataFormat.getPrefix()), false);
    }

    @Override
    public MetaRepo.MetadataFormat getMetadataFormat() {
        return metadataFormat;
    }

    @Override
    public RecordMapping getRecordMapping() {
        try {
            return RecordMapping.read((String) object.get(RECORD_MAPPING), implFactory.getMetadataModel());
        }
        catch (MetadataException e) {
            throw new MetaRepoSystemException("Cannot read recor mapping", e);
        }
    }

    @Override
    public int compareTo(MetaRepo.Mapping o) {
        return getMetadataFormat().getPrefix().compareTo(o.getMetadataFormat().getPrefix());
    }

    @Override
    public void executeMapping(List<? extends MetaRepo.Record> records, Map<String, String> namespaces) throws MappingNotFoundException {
        try {
            MappingRunner mappingRunner = getMappingRunner();
            MetadataRecord.Factory factory = new MetadataRecord.Factory(namespaces);
            int invalidCount = 0;
            Iterator<? extends MetaRepo.Record> walk = records.iterator();
            while (walk.hasNext()) {
                MetaRepo.Record record = walk.next();
                try {
                    MetadataRecord metadataRecord = factory.fromXml(record.getXmlString(dataSet.getDetails().getMetadataFormat().getPrefix()));
                    String recordString = mappingRunner.runMapping(metadataRecord);
                    List<String> problems = new ArrayList<String>();
                    String validated = recordValidator.validateRecord(recordString, problems);
                    if (problems.isEmpty()) {
                        RecordImpl recordImpl = (RecordImpl) record;
                        recordImpl.addFormat(getMetadataFormat(), validated);
                    }
                    else {
                        log.info("invalid record: " + recordString);
                        invalidCount++;
                        walk.remove(); // todo: separate fetching from mapping
                    }
                }
                catch (MappingException e) {
                    log.warn("mapping exception: " + e);
                    invalidCount++;
                    walk.remove();
                }
                catch (XMLStreamException e) {
                    log.warn("Unable to map record!", e);
                }
            }
            if (invalidCount > 0) {
                log.info(String.format("%d invalid records discarded", invalidCount));
            }
        }
        catch (MetadataException e) {
            log.error("Metadata exception!", e);
            throw new MetaRepoSystemException("Unable to read metadata mapping", e);
        }
        // todo break here if mapping is consistently invalidating all records.
    }

    private MappingRunner getMappingRunner() throws MetadataException {
        if (mappingRunner == null) {
            RecordMapping recordMapping = getRecordMapping();
            String compileCode = recordMapping.toCompileCode(implFactory.getMetadataModel());
            mappingRunner = new MappingRunner(implFactory.getGroovyCodeResource(), compileCode);
        }
        return mappingRunner;
    }
}


//    // for now we pretend that we have an ESE mapping
//
//    private class FakeESEMappingImpl implements Mapping, MappingInternal, Comparable<Mapping> {
//        private final Set<String> ELIMINATE = new TreeSet<String>(Arrays.asList(
//                "uri",
//                "collectionName",
//                "collectionTitle",
//                "hasObject",
//                "language",
//                "country"
//        ));
//        private MappingInternal mappingInternal;
//        private ESEStripper eseStripper = new ESEStripper();
//        private ESEMetadataFormat eseMetadataFormat = new ESEMetadataFormat();
//
//        private FakeESEMappingImpl(MappingInternal mappingInternal) {
//            this.mappingInternal = mappingInternal;
//        }
//
//        @Override
//        public MetadataFormat getMetadataFormat() {
//            return eseMetadataFormat;
//        }
//
//        @Override
//        public RecordMapping getRecordMapping() {
//            return null;  // there is none!
//        }
//
//        @Override
//        public int compareTo(Mapping o) {
//            return getMetadataFormat().getPrefix().compareTo(o.getMetadataFormat().getPrefix());
//        }
//
//        @Override
//        public void executeMapping(List<? extends Record> records, Map<String, String> namespaces) throws MappingNotFoundException {
//            mappingInternal.executeMapping(records, namespaces);
//            Iterator<? extends Record> recordWalk = records.iterator();
//            while (recordWalk.hasNext()) {
//                Record record = recordWalk.next();
//                String stripped = eseStripper.strip(record.getXmlString(MAPPED_NAMESPACE.getPrefix()));
//                if (!stripped.contains("<europeana:object>")) {
//                    recordWalk.remove();
//                }
//                else {
//                    ((RecordImpl) record).addFormat(getMetadataFormat(), stripped);
//                }
//            }
//        }
//
//        private class ESEStripper {
//            private String context;
//            private int contextBegin, contextEnd;
//
//            private ESEStripper() {
//                StringBuilder contextString = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n\n<strip\n");
//                for (NamespaceDefinition ns : metadataModel.getRecordDefinition().namespaces) {
//                    contextString.append(String.format("xmlns:%s=\"%s\"\n", ns.prefix, ns.uri));
//                }
//                contextString.append(">\n%s</strip>\n");
//                this.context = contextString.toString();
//                this.contextBegin = this.context.indexOf("%s");
//                this.contextEnd = this.context.length() - (this.contextBegin + 2);
//            }
//
//            public String strip(String recordString) {
//                String contextualizedRecord = String.format(context, recordString);
//                StringWriter out = new StringWriter();
//                try {
//                    Document document = DocumentHelper.parseText(contextualizedRecord);
//                    stripDocument(document);
//                    OutputFormat format = OutputFormat.createPrettyPrint();
//                    XMLWriter writer = new XMLWriter(out, format);
//                    writer.write(document);
//                }
//                catch (Exception e) {
//                    throw new RuntimeException("Unable to strip for ESE");
//                }
//                out.getBuffer().delete(0, contextBegin);
//                out.getBuffer().delete(out.getBuffer().length() - contextEnd, out.getBuffer().length());
//                return out.toString();
//            }
//
//            private void stripDocument(Document document) {
//                Element validateElement = document.getRootElement();
//                Element recordElement = validateElement.element("record");
//                if (recordElement == null) {
//                    throw new RuntimeException("Cannot find record element");
//                }
//                stripElement(recordElement, new Path());
//            }
//
//            private boolean stripElement(Element element, Path path) {
//                path.push(Tag.create(element.getNamespacePrefix(), element.getName()));
//                boolean hasElements = false;
//                Iterator walk = element.elementIterator();
//                while (walk.hasNext()) {
//                    Element subelement = (Element) walk.next();
//                    boolean remove = stripElement(subelement, path);
//                    if (remove) {
//                        walk.remove();
//                    }
//                    hasElements = true;
//                }
//                if (!hasElements) {
//                    FieldDefinition fieldDefinition = metadataModel.getRecordDefinition().getFieldDefinition(path);
//                    if (fieldDefinition == null) {
//                        throw new RuntimeException("Should have found field definition");
//                    }
//                    path.pop();
//                    return fieldDefinition.getPrefix().equals(MAPPED_NAMESPACE.getPrefix()) ||
//                            fieldDefinition.getPrefix().equals("europeana") && ELIMINATE.contains(fieldDefinition.getLocalName());
//                }
//                path.pop();
//                return false;
//
//            }
//        }
//
//        private class ESEMetadataFormat implements MetadataFormat {
//
//            @Override
//            public String getPrefix() {
//                return "ese";
//            }
//
//            @Override
//            public void setPrefix(String value) {
//                throw new RuntimeException();
//            }
//
//            @Override
//            public String getSchema() {
//                return "http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd";
//            }
//
//            @Override
//            public void setSchema(String value) {
//                throw new RuntimeException();
//            }
//
//            @Override
//            public String getNamespace() {
//                return "http://www.europeana.eu/schemas/ese/";
//            }
//
//            @Override
//            public void setNamespace(String value) {
//                throw new RuntimeException();
//            }
//
//            @Override
//            public boolean isAccessKeyRequired() {
//                return false;  // ESE is free-for-all
//            }
//
//            @Override
//            public void setAccessKeyRequired(boolean required) {
//                // do nothing
//            }
//        }
//    }

