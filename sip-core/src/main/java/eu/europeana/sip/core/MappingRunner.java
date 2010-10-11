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

package eu.europeana.sip.core;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.StringWriter;

/**
 * This class takes code, a record, and produces a record, using the code
 * as the mapping.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MappingRunner {
    private String code;
    private Script script;
    private ConstantFieldModel constantFieldModel;

    public MappingRunner(String code, ConstantFieldModel constantFieldModel) {
        this.code = code;
        this.constantFieldModel = constantFieldModel;
    }

    public String runMapping(MetadataRecord metadataRecord) throws MappingException {
        if (metadataRecord == null) {
            throw new RuntimeException("Null input metadata record");
        }
        try {
            Binding binding = new Binding();
            StringWriter writer = new StringWriter();
            MarkupBuilder builder = new MarkupBuilder(writer);
            NamespaceBuilder xmlns = new NamespaceBuilder(builder);
            binding.setVariable("output", builder);
            binding.setVariable("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
            binding.setVariable("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
            binding.setVariable("europeana", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
            binding.setVariable("icn", xmlns.namespace("http://www.icn.nl/", "icn"));
            for (ConstantFieldModel.FieldSpec fieldSpec : constantFieldModel.getFields()) {
                binding.setVariable(fieldSpec.getName(), constantFieldModel.get(fieldSpec.getName()));
            }
            binding.setVariable("input", metadataRecord.getRootNode());
            if (script == null) {
                script = new GroovyShell(binding).parse(code);
            }
            else {
                script.setBinding(binding);
            }
            script.run();
            return writer.toString();
        }
        catch (MissingPropertyException e) {
            throw new MappingException(metadataRecord, "Missing Property "+e.getProperty(), e);
        }
        catch (MultipleCompilationErrorsException e) {
            StringBuilder out = new StringBuilder();
            for (Object o : e.getErrorCollector().getErrors()) {
                SyntaxErrorMessage message = (SyntaxErrorMessage) o;
                @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"}) SyntaxException se = message.getCause();
                // line numbers will not match
                out.append(String.format("Problem: %s\n", se.getOriginalMessage()));
            }
            throw new MappingException(metadataRecord, out.toString(), e);
        }
        catch (Exception e) {
            throw new MappingException(metadataRecord, "Unexpected", e);
        }
    }
}
