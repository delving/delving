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

import eu.europeana.sip.xml.Sanitizer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Make sure the field mapping interprets code correctly
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestFieldMapping {

    @Test
    public void extractVariables() {
        FieldMapping fm = new FieldMapping(null);
        fm.setCode("whatever code contains a variable like input.something.something should reveal that");
        List<String> vars = fm.getVariables();
        assertEquals(1, vars.size());
        assertEquals("input.something.something", vars.get(0));
    }

    @Test
    public void extractMultiple() {
        FieldMapping fm = new FieldMapping(null);
        fm.setCode("suppose \ninput.something.something were to appear on the same line as input.somethingelse,\n kind of");
        List<String> vars = fm.getVariables();
        assertEquals(2, vars.size());
        assertEquals("input.something.something", vars.get(0));
        assertEquals("input.somethingelse", vars.get(1));
    }

    @Test
    public void strangeCharacters() {
        FieldMapping fm = new FieldMapping(null);
        fm.setCode("did you know that accents were possible? \n"+ Sanitizer.tagToVariable("input.strange.ANN\u00C8E_DE_D\u00C8BUT_DE_FABRICATION")+" is a legit var name");
        List<String> vars = fm.getVariables();
        assertEquals(1, vars.size());
        assertEquals("input.strange.ANNEE_DE_DEBUT_DE_FABRICATION", vars.get(0));
    }
}
