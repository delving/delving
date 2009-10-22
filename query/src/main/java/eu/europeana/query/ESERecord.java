/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or as soon they
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

package eu.europeana.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Store the map of record field to string value, where keys can appear multiple times
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ESERecord implements Iterable<ESERecord.Field> {
    private List<Field> fields = new ArrayList<Field>();

    public void put(RecordField key, String value) {
        fields.add(new Field(key, value));
    }

    public Iterator<Field> iterator() {
        return fields.iterator();
    }

    public static class Field {
        private RecordField key;
        private String value;

        public Field(RecordField key, String value) {
            this.key = key;
            this.value = value;
        }

        public RecordField getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}