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
import eu.europeana.sip.xml.QNameBuilder;
import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Writer;

/**
 * Use the normalization parser as a source of binding objects for the groovy script execution
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizationParserBindingSource implements GroovyService.BindingSource {
    private NormalizationParser normalizationParser;
    private GroovyNode record;

    public void prepareInputFile(File inputFile) throws XMLStreamException, FileNotFoundException {
        if (normalizationParser != null) {
            normalizationParser.close();
        }
        QName recordRoot = QNameBuilder.createQName("record");
        normalizationParser = new NormalizationParser(new FileInputStream(inputFile), recordRoot);
        nextRecord();
    }

    void nextRecord() {
        try {
            record = normalizationParser.nextRecord();
        }
        catch (Exception e) {
//            next.setEnabled(false);
        }
    }

    @Override
    public Binding createBinding(Writer writer) {
        MarkupBuilder builder = new MarkupBuilder(writer);
        NamespaceBuilder xmlns = new NamespaceBuilder(builder);
        Binding binding = new Binding();
        binding.setVariable("input", record);
        binding.setVariable("output", builder);
        binding.setVariable("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
        binding.setVariable("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
        binding.setVariable("europeana", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
        return binding;
    }
}

