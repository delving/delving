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

package eu.europeana.sip.model;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * The groovy code that is to validate records
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ValidationCodeModel {
    private static final String HEADER = "// ValidationCode.groovy - the place for record validation\n\n";
    private static final String FILE_NAME = "ValidationCode.groovy";
    private static final File CODE_FILE = new File(FILE_NAME);
    private static final URL CODE_RESOURCE = ValidationCodeModel.class.getResource("/" + FILE_NAME);
    private String resourceCode;
    private String fileCode;
    private long fileModified;
    private Script script;

    public ValidationCodeModel() {
        try {
            resourceCode = readResourceCode();
            if (!CODE_FILE.exists()) {
                FileWriter out = new FileWriter(CODE_FILE);
                out.write(HEADER);
                out.close();
            }
        }
        catch (IOException e) {
            resourceCode = "println 'Could not read tool code: " + e.toString() + "'";
        }
    }

    public Script getScript() {
        if (script == null) {
            script = new GroovyShell().parse(getCode());
        }
        return script;
    }

    public String getCode() {
        try {
            long mod = CODE_FILE.lastModified();
            if (mod > fileModified) {
                fileCode = readFileCode();
                fileModified = mod;
            }
            if (fileCode.isEmpty()) {
                return fileCode;
            }
            else {
                return resourceCode;
            }
        }
        catch (IOException e) {
            return "println 'Could not read validation code'";
        }
    }

    private String readFileCode() throws IOException {
        return readCode(new FileReader(CODE_FILE));
    }

    private String readResourceCode() throws IOException {
        if (CODE_RESOURCE == null) {
            throw new IOException("Cannot find resource");
        }
        InputStream in = CODE_RESOURCE.openStream();
        Reader reader = new InputStreamReader(in);
        return readCode(reader);
    }

    private String readCode(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            out.append(line).append('\n');
        }
        in.close();
        return out.toString();
    }
}