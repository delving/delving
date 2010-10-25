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

import eu.delving.core.metadata.RecordMapping;
import eu.europeana.sip.core.FieldEntry;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCode;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Take the input and config informationm and produce an output xml file
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Normalizer implements Runnable {
    private SipModel sipModel;
    private boolean discardInvalid;
    private boolean storeNormalizedFile;
    private MetadataParser.Listener parserListener;
    private Listener listener;
    private volatile boolean running = true;

    public interface Listener {
        void invalidInput(MappingException exception);

        void invalidOutput(RecordValidationException exception);

        void finished(boolean success);
    }

    public Normalizer(
            SipModel sipModel,
            boolean discardInvalid,
            boolean storeNormalizedFile,
            MetadataParser.Listener parserListener,
            Listener listener
    ) {
        this.sipModel = sipModel;
        this.discardInvalid = discardInvalid;
        this.storeNormalizedFile = storeNormalizedFile;
        this.parserListener = parserListener;
        this.listener = listener;
    }

    public void run() {
        try {
            RecordMapping recordMapping = sipModel.getFileSet().getMapping();
            ToolCode toolCode = new ToolCode();
            FileSet.Output fileSetOutput = sipModel.getFileSet().prepareOutput(storeNormalizedFile);
            if (storeNormalizedFile) {
                fileSetOutput.getOutputWriter().write("<?xml version='1.0' encoding='UTF-8'?>\n");
                fileSetOutput.getOutputWriter().write("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:europeana=\"http://www.europeana.eu\" xmlns:dcterms=\"http://purl.org/dc/terms/\">\n");
            }
            MappingRunner mappingRunner = new MappingRunner(toolCode.getCode() + recordMapping.toCode(sipModel.getMetadataModel().getRecordDefinition(), null));
            MetadataParser parser = new MetadataParser(sipModel.getFileSet().getInputStream(), recordMapping.getRecordRoot(), parserListener);
            RecordValidator recordValidator = new RecordValidator(sipModel.getMetadataModel(), true);
            MetadataRecord record;
            while ((record = parser.nextRecord()) != null && running) {
                try {
                    String output = mappingRunner.runMapping(record);
                    List<FieldEntry> fieldEntries = FieldEntry.createList(output);
                    recordValidator.validate(record, fieldEntries);
                    String validated = FieldEntry.toString(fieldEntries, true);
                    if (storeNormalizedFile) {
                        fileSetOutput.getOutputWriter().write(validated);
                    }
                    fileSetOutput.recordNormalized();
                }
                catch (MappingException e) {
                    if (discardInvalid && fileSetOutput.getDiscardedWriter() != null) {
                        try {
                            fileSetOutput.getDiscardedWriter().write(record.toString());
                            e.printStackTrace(new PrintWriter(fileSetOutput.getDiscardedWriter()));
                            fileSetOutput.getDiscardedWriter().write("\n========================================\n");
                            fileSetOutput.recordDiscarded();
                        }
                        catch (IOException e1) {
                            sipModel.tellUser("Unable to write discarded record", e1);
                        }
                    }
                    else {
                        sipModel.tellUser("Problem normalizing " + record.toString(), e);
                        listener.invalidInput(e);
                        fileSetOutput.close(true);
                        running = false;
                    }
                }
                catch (RecordValidationException e) {
                    if (discardInvalid && fileSetOutput.getDiscardedWriter() != null) {
                        try {
                            fileSetOutput.getDiscardedWriter().write(record.toString());
                            e.printStackTrace(new PrintWriter(fileSetOutput.getDiscardedWriter()));
                            fileSetOutput.getDiscardedWriter().write("\n========================================\n");
                            fileSetOutput.recordDiscarded();
                        }
                        catch (IOException e1) {
                            sipModel.tellUser("Unable to write discarded record", e1);
                        }
                    }
                    else {
                        sipModel.tellUser("Invalid output record", e);
                        listener.invalidOutput(e);
                        fileSetOutput.close(true);
                        running = false;
                    }
                }
                catch (Exception e) {
                    sipModel.tellUser("Problem writing output", e);
                    running = false;
                }
            }
            if (storeNormalizedFile) {
                fileSetOutput.getOutputWriter().write("</metadata>\n");
            }
            fileSetOutput.close(!running);
            if (!running) {
                parserListener.recordsParsed(0, true);
            }
            listener.finished(running);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("XML Problem", e);
        }
        catch (IOException e) {
            throw new RuntimeException("IO Problem", e);
        }
    }

    public void abort() {
        running = false;
    }
}
