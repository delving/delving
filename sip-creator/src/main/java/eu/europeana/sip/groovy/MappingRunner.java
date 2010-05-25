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

package eu.europeana.sip.groovy;

import eu.europeana.sip.model.GlobalField;
import eu.europeana.sip.model.GlobalFieldModel;
import eu.europeana.sip.xml.MetadataRecord;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This class takes code, a record, and produces a record, using the code
 * as the mapping.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MappingRunner {
    private String code;
    private Script script;
    private GlobalFieldModel globalFieldModel;
    private Listener listener;

    public interface Listener {
        void complete(Exception exception, String output);
    }

    public MappingRunner(String code, GlobalFieldModel globalFieldModel, Listener listener) {
        this.code = code;
        this.globalFieldModel = globalFieldModel;
        this.listener = listener;
    }

    public void compile(MetadataRecord metadataRecord) {
        try {
            StringWriter writer = new StringWriter();
            Binding binding = createBinding(writer, globalFieldModel, metadataRecord);
            if (script == null) {
                script = new GroovyShell(binding).parse(code);
            }
            else {
                script.setBinding(binding);
            }
            script.run();
            listener.complete(null, writer.toString());
        }
        catch (MissingPropertyException e) {
            listener.complete(e, "Missing Property: " + e.getProperty());
        }
        catch (MultipleCompilationErrorsException e) {
            StringBuilder out = new StringBuilder();
            for (Object o : e.getErrorCollector().getErrors()) {
                SyntaxErrorMessage message = (SyntaxErrorMessage) o;
                SyntaxException se = message.getCause();
                // line numbers will not match
                out.append(String.format("Problem: %s", se.getOriginalMessage()));
            }
            listener.complete(e, out.toString());
        }
        catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            listener.complete(e, writer.toString());
        }
    }

    private static Binding createBinding(Writer writer, GlobalFieldModel globalFieldModel, MetadataRecord record) {
        Binding binding = new Binding();
        MarkupBuilder builder = new MarkupBuilder(writer);
        NamespaceBuilder xmlns = new NamespaceBuilder(builder);
        binding.setVariable("output", builder);
        binding.setVariable("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
        binding.setVariable("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
        binding.setVariable("europeana", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
        for (GlobalField globalField : GlobalField.values()) {
            binding.setVariable(globalField.getVariableName(), globalFieldModel.get(globalField));
        }
        binding.setVariable("input", record.getRootNode());
        return binding;
    }
}
