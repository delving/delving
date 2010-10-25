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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * The groovy helper code that precedes the mapping snippet.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ToolCode {
    private static final URL CODE_RESOURCE = ToolCode.class.getResource("/ToolCode.groovy");
    private String resourceCode;

    public ToolCode() {
        try {
            resourceCode = readResourceCode();
        }
        catch (IOException e) {
            resourceCode = "println 'Could not read tool code: " + e.toString() + "'";
        }
    }

    public String getCode() {
        return resourceCode;
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
