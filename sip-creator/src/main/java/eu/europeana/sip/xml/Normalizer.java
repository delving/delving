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

import eu.europeana.sip.groovy.MappingScriptBinding;
import eu.europeana.sip.model.FileSet;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Take the input and config informationm and produce an output xml file
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Normalizer implements Runnable {
    private Logger log = Logger.getLogger(getClass());
    private FileSet fileSet;
    private boolean running;

    public Normalizer(FileSet fileSet) {
        this.fileSet = fileSet;
    }

    public void run() {
        try {
            QName recordRoot = fileSet.getRecordRoot();
            InputStream inputStream = fileSet.getInputStream();
            OutputStream outputStream = fileSet.getOutputStream();
            String mapping = fileSet.getMapping();
            Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
            MappingScriptBinding mappingScriptBinding = new MappingScriptBinding(writer);
            MetadataParser parser = new MetadataParser(inputStream, recordRoot);
            GroovyShell shell = new GroovyShell(mappingScriptBinding);
            Script script = shell.parse(mapping);
            script.setBinding(mappingScriptBinding);
            MetadataRecord record;
            int count = 0;
            running = true;
            while ((record = parser.nextRecord()) != null && running) {
                if (count % 100 == 0) {
                    log.info("record "+count);
                }
                mappingScriptBinding.setRecord(record);
                script.run();
                count++;
            }
            writer.close();
            if (!running) {
                parser.close();
            }
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
