/*
 * Copyright 2007 EDL FOUNDATION
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

package eu.europeana.sip.xml;

import eu.delving.metadata.MetadataNamespace;
import eu.delving.metadata.Path;
import eu.delving.metadata.RecordMapping;
import eu.delving.metadata.RecordValidator;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.ToolCodeResource;
import eu.europeana.sip.model.SipModel;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Take the input and config informationm and produce an output xml file
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Normalizer implements Runnable {
    private SipModel sipModel;
    private Path recordRoot;
    private int recordCount;
    private boolean discardInvalid;
    private File normalizedFile;
    private ProgressListener progressListener;
    private Listener listener;

    public interface Listener {
        void invalidInput(MappingException exception);

        void invalidOutput(RecordValidationException exception);

        void finished(boolean success);
    }

    public Normalizer(
            SipModel sipModel,
            Path recordRoot,
            int recordCount,
            boolean discardInvalid,
            File normalizedFile,
            ProgressListener progressListener,
            Listener listener
    ) {
        this.sipModel = sipModel;
        this.recordRoot = recordRoot;
        this.recordCount = recordCount;
        this.discardInvalid = discardInvalid;
        this.normalizedFile = normalizedFile;
        this.progressListener = progressListener;
        this.listener = listener;
    }

    public void run() {
        FileStore.MappingOutput fileSetOutput = null;
        boolean store = normalizedFile != null;
        ProgressAdapter progressAdapter = new ProgressAdapter(progressListener);
        try {
            RecordMapping recordMapping = sipModel.getMappingModel().getRecordMapping();
            if (recordMapping == null) {
                return;
            }
            ToolCodeResource toolCodeResource = new ToolCodeResource();
            fileSetOutput = sipModel.getDataSetStore().createMappingOutput(recordMapping, normalizedFile);
            if (store) {
                Writer out = fileSetOutput.getOutputWriter();
                out.write("<?xml version='1.0' encoding='UTF-8'?>\n");
                out.write("<metadata");
                writeNamespace(out, MetadataNamespace.DC);
                writeNamespace(out, MetadataNamespace.DCTERMS);
                writeNamespace(out, MetadataNamespace.EUROPEANA);
                out.write(">\n");
            }
            MappingRunner mappingRunner = new MappingRunner(toolCodeResource.getCode() + recordMapping.toCompileCode(sipModel.getMetadataModel().getRecordDefinition()));
            MetadataParser parser = new MetadataParser(sipModel.getDataSetStore().createXmlInputStream(), recordRoot, recordCount, progressAdapter);
            RecordValidator recordValidator = new RecordValidator(sipModel.getMetadataModel(), true);
            MetadataRecord record;
            while ((record = parser.nextRecord()) != null && progressAdapter.running) {
                try {
                    String output = mappingRunner.runMapping(record);
                    List<String> problems = new ArrayList<String>();
                    String validated = recordValidator.validateRecord(output, problems);
                    if (problems.isEmpty()) {
                        if (store) {
                            fileSetOutput.getOutputWriter().write(validated);
                        }
                        fileSetOutput.recordNormalized();
                    }
                    else {
                        throw new RecordValidationException(record, problems);
                    }
                }
                catch (MappingException e) {
                    if (discardInvalid) {
                        if (store) {
                            try {
                                fileSetOutput.getDiscardedWriter().write(record.toString());
                                e.printStackTrace(new PrintWriter(fileSetOutput.getDiscardedWriter()));
                                fileSetOutput.getDiscardedWriter().write("\n========================================\n");
                                fileSetOutput.recordDiscarded();
                            }
                            catch (IOException e1) {
                                sipModel.getUserNotifier().tellUser("Unable to write discarded record", e1);
                            }
                        }
                    }
                    else {
                        sipModel.getUserNotifier().tellUser("Problem normalizing " + record.toString(), e);
                        listener.invalidInput(e);
                        if (store) {
                            fileSetOutput.close(true);
                        }
                        progressAdapter.running = false;
                    }
                }
                catch (RecordValidationException e) {
                    if (discardInvalid) {
                        if (store) {
                            try {
                                fileSetOutput.getDiscardedWriter().write(record.toString());
                                e.printStackTrace(new PrintWriter(fileSetOutput.getDiscardedWriter()));
                                fileSetOutput.getDiscardedWriter().write("\n========================================\n");
                                fileSetOutput.recordDiscarded();
                            }
                            catch (IOException e1) {
                                sipModel.getUserNotifier().tellUser("Unable to write discarded record", e1);
                            }
                        }
                    }
                    else {
                        sipModel.getUserNotifier().tellUser("Invalid output record", e);
                        listener.invalidOutput(e);
                        if (store) {
                            fileSetOutput.close(true);
                        }
                        progressAdapter.running = false;
                    }
                }
                catch (Exception e) {
                    sipModel.getUserNotifier().tellUser("Problem writing output", e);
                    progressAdapter.running = false;
                }
            }
            if (store) {
                fileSetOutput.getOutputWriter().write("</metadata>\n");
            }
            fileSetOutput.close(!progressAdapter.running);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("XML Problem", e);
        }
        catch (IOException e) {
            throw new RuntimeException("IO Problem", e);
        }
        catch (FileStoreException e) {
            throw new RuntimeException("Datastore Problem", e);
        }
        catch (MetadataParser.AbortException e) {
            if (fileSetOutput != null) {
                try {
                    fileSetOutput.close(true);
                }
                catch (FileStoreException e1) {
                    throw new RuntimeException("Couldn't close output properly");
                }
            }
        }
        finally {
            listener.finished(progressAdapter.running);
        }
    }

    private void writeNamespace(Writer writer, MetadataNamespace namespace) throws IOException {
        writer.write(String.format(" xmlns:%s=\"%s\"", namespace.getPrefix(), namespace.getUri()));
    }

    private class ProgressAdapter implements ProgressListener {
        private volatile boolean running = true;
        private ProgressListener progressListener;

        private ProgressAdapter(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void setTotal(int total) {
            progressListener.setTotal(total);
        }

        @Override
        public boolean setProgress(int progress) {
            boolean proceed = progressListener.setProgress(progress);
            if (!proceed) {
                running = false;
            }
            return running && proceed;
        }

        @Override
        public void finished() {
            progressListener.finished();
        }
    }
}
