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

package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyService;
import eu.europeana.sip.xml.GroovyNode;
import eu.europeana.sip.xml.NormalizationParser;
import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Use the normalization parser as a source of binding objects for the groovy script execution
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizationParserBindingSource implements GroovyService.BindingSource {
    private final static Logger LOG = Logger.getLogger(NormalizationParserBindingSource.class);
    private NormalizationParser normalizationParser;
    private GroovyNode record;
    private Listener listener;

    public interface Listener {
        void updateAvailableNodes(List<String> groovyNodes);
    }

    public NormalizationParserBindingSource(Listener listener) {
        this.listener = listener;
    }

    public void prepareInputFile(File inputFile, QName recordRoot) throws XMLStreamException, FileNotFoundException {
        if (normalizationParser != null) {
            normalizationParser.close();
        }
        normalizationParser = new NormalizationParser(new FileInputStream(inputFile), recordRoot);
        nextRecord();
    }

    @SuppressWarnings("unchecked")
    void nextRecord() {
        try {
            record = normalizationParser.nextRecord();
            if (null == record) {
                return;
            }
            listener.updateAvailableNodes(record.names());
        }
        catch (XMLStreamException e) {
            LOG.error("XML stream error", e);
        }
        catch (IOException e) {
            LOG.error("IO error", e);
        }
//            next.setEnabled(false);    // todo: activate
    }

    @Override
    public Binding createBinding(Writer writer) {
        MarkupBuilder builder = new MarkupBuilder(writer);
        NamespaceBuilder xmlns = new NamespaceBuilder(builder);
        Binding binding = new Binding();
        binding.setVariable(INPUT, record);
        binding.setVariable(OUTPUT, builder);
        binding.setVariable(DC, xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
        binding.setVariable(DCTERMS, xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
        binding.setVariable(EUROPEANA, xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
        return binding;
    }
}

