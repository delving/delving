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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Make sure the value map model (de)serializes correctly
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestValueMapModel {

    @Test
    public void extractVariables() {
        ValueMapModel model = ValueMapModel.fromMapping(
                        "/* ValueMap */ def gumby = [ // one,two,three\n" +
                        "'uno':'one'\n" +
                        "'duo':'two'\n" +
                        "'trezz':'three'\n" +
                        "]\n" +
                        "/* ValueMap */ def pokey = [ // no,yes\n" +
                        "'fantastic':'yes'\n" +
                        "'terrible':'no'\n" +
                        "]\n"
        );
        assertEquals(2, model.getMaps().size());
        String stringForm = model.toString();
        System.out.println(stringForm);
        ValueMapModel model2 = ValueMapModel.fromMapping(stringForm);
        assertEquals(2, model2.getMaps().size());
        assertEquals("yes", model2.getMaps().get("pokey").get("fantastic"));
        model2.getMaps().get("gumby").put("whatever", "two");
        try {
            model2.getMaps().get("gumby").put("noway", "six");
            fail();
        }
        catch (RuntimeException e) {
            // okay!
        }
    }
}
