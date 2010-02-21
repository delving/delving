/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.querymodel.beans;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class BeanUtil {
    private static final String[] STRINGS = new String[]{" "};
    private static final String EMPTY_STRING = " ";

    static String returnStringOrElse(String[] s) {
        return (s != null) ? s[0] : EMPTY_STRING;
    }

    static String returnStringOrElse(String s) {
        return (s != null) ? s : EMPTY_STRING;
    }

    static String[] returnArrayOrElse(String[] s) {
        return (s != null) ? s : STRINGS;
    }

    static String[] returnArrayOrElse (String[] ... arrs) {
        for (String[] arr : arrs) {
            if (arr != null) {
                return arr;
            }
        }
        return STRINGS;
    }


}