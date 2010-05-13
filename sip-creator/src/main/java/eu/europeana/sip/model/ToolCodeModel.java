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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The groovy helper code that precedes the mapping snippet.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ToolCodeModel {
    private static final String FILE_NAME = "ToolCode.groovy";

    public static String getToolCode() {
        try {
            return readToolCode();
        }
        catch (IOException e) {
            return "print 'Could not read tool code'";
        }
    }

    private static String readToolCode() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            FileWriter out = new FileWriter(file);
            out.write("// ToolCode.groovy - the place for helpful closures\n\n");
            out.close();
        }
        System.out.println("modified "+(System.currentTimeMillis()-file.lastModified()));
        BufferedReader in = new BufferedReader(new FileReader(file));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            out.append(line).append('\n');
        }
        in.close();
        return out.toString();
    }
}
