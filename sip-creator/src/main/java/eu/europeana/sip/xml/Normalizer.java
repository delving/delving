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

import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordRoot;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCodeModel;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.UserNotifier;
import groovy.lang.MissingPropertyException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Take the input and config informationm and produce an output xml file
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Normalizer implements Runnable {
    private FileSet fileSet;
    private FileSet.Output fileSetOutput;
    private AnnotationProcessor annotationProcessor;
    private RecordValidator recordValidator;
    private boolean discardInvalid;
    private MetadataParser.Listener parserListener;
    private Listener listener;
    private UserNotifier userNotifier;
    private boolean running = true;

    public interface Listener {
        void invalidInput(MetadataRecord metadataRecord, MissingPropertyException exception);

        void invalidOutput(RecordValidationException exception);

        void finished(boolean success);
    }

    public Normalizer(
            FileSet fileSet,
            AnnotationProcessor annotationProcessor,
            RecordValidator recordValidator,
            boolean discardInvalid,
            UserNotifier userNotifier,
            MetadataParser.Listener parserListener,
            Listener listener
    ) {
        this.fileSet = fileSet;
        this.annotationProcessor = annotationProcessor;
        this.recordValidator = recordValidator;
        this.discardInvalid = discardInvalid;
        this.userNotifier = userNotifier;
        this.parserListener = parserListener;
        this.listener = listener;
    }

    public void run() {
        try {
            String mapping = fileSet.getMapping();
            RecordRoot recordRoot = RecordRoot.fromMapping(mapping);
            ConstantFieldModel constantFieldModel = ConstantFieldModel.fromMapping(mapping, annotationProcessor);
            ToolCodeModel toolCodeModel = new ToolCodeModel();
            fileSetOutput = fileSet.prepareOutput();
            final Writer outputWriter = new OutputStreamWriter(fileSetOutput.getOutputStream(), "UTF-8");
            final Writer discardedWriter = discardInvalid ? new OutputStreamWriter(fileSetOutput.getDiscardedStream(), "UTF-8") : null;
            outputWriter.write("<?xml version='1.0' encoding='UTF-8'?>\n");
            outputWriter.write("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:europeana=\"http://www.europeana.eu\" xmlns:dcterms=\"http://purl.org/dc/terms/\">\n");
            MappingRunner mappingRunner = new MappingRunner(toolCodeModel.getCode() + mapping, constantFieldModel, new MappingRunner.Listener() {
                @Override
                public void complete(MetadataRecord metadataRecord, Exception exception, String output) {
                    if (exception != null) {
                        if (discardedWriter != null) {
                            try {
                                discardedWriter.write(metadataRecord.toString());
                                fileSetOutput.recordDiscarded();
                            }
                            catch (IOException e1) {
                                userNotifier.tellUser("Unable to write discarded record", e1);
                            }
                        }
                        else {
                            if (exception instanceof MissingPropertyException) {
                                MissingPropertyException mpe = (MissingPropertyException) exception;
                                userNotifier.tellUser("Missing property in record " + metadataRecord.getRecordNumber() + ": " + mpe.getProperty(), exception);
                                listener.invalidInput(metadataRecord, mpe);
                            }
                            else {
                                userNotifier.tellUser("Problem normalizing record " + metadataRecord.toString(), exception);
                            }
                            fileSetOutput.close(true);
                        }
                    }
                    else {
                        try {
                            String validated = recordValidator.validate(metadataRecord, output);
                            outputWriter.write(validated);
                            fileSetOutput.recordNormalized();
                        }
                        catch (RecordValidationException e) {
                            if (discardedWriter != null) {
                                try {
                                    discardedWriter.write(metadataRecord.toString());
                                    fileSetOutput.recordDiscarded();
                                }
                                catch (IOException e1) {
                                    userNotifier.tellUser("Unable to write discarded record", e1);
                                }
                            }
                            else {
                                userNotifier.tellUser("Invalid output record", e);
                                listener.invalidOutput(e);
                                fileSetOutput.close(true);
                                running = false;
                            }
                        }
                        catch (Exception e) {
                            userNotifier.tellUser("Problem writing output", e);
                            running = false;
                        }
                    }
                }
            });
            MetadataParser parser = new MetadataParser(fileSet.getInputStream(), recordRoot, parserListener);
            MetadataRecord record;
            while ((record = parser.nextRecord()) != null && running) {
                if (!mappingRunner.runMapping(record) && !discardInvalid) {
                    running = false;
                }
            }
            outputWriter.write("</metadata>\n");
            if (discardedWriter != null) {
                discardedWriter.close();
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
