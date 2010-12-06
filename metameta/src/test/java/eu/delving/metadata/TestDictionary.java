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

package eu.delving.metadata;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Make sure the value map model (de)serializes correctly
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestDictionary {

    @Test
    public void writeAndRead() {
        Map<String, Dictionary> maps = Dictionary.fromMapping(toLines(
                        "/* Dictionary */ def gumbyMap = [ // one,two,three\n" +
                        "'uno':'one',\n" +
                        "'duo':'two',\n" +
                        "'trezz':'three',\n" +
                        "]\n" +
                        "/* Dictionary */ def pokeyMap = [ // no,yes\n" +
                        "'fantastic':'yes',\n" +
                        "'terrible':'no',\n" +
                        "]\n"
        ));
        assertEquals(2, maps.size());
        StringBuilder out = new StringBuilder();
        for (Dictionary dictionary : maps.values()) {
            out.append(dictionary.toString());
        }
        System.out.println(out);
        maps = Dictionary.fromMapping(toLines(out.toString()));
        assertEquals(2, maps.size());
        assertEquals("yes", maps.get("pokey").get("fantastic"));
        maps.get("gumby").put("whatever", "two");
        try {
            maps.get("gumby").put("noway", "six");
            fail();
        }
        catch (RuntimeException e) {
            // okay!
        }
    }

    private List<String> toLines(String code) {
        return Arrays.asList(code.split("\n"));
    }
}
