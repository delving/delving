/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.delving.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class LocalizedFieldNames {
    private static final String METADATA_KEY_PREFIX = "_metadata.";

    @Autowired
    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public interface Lookup {
        String toLocalizedName(String fieldName, Locale locale);
        String toFieldName(String localizedName, Locale locale);
    }

    public Lookup createLookup(List<String> keys) {
        return new LookupImpl(keys);
    }

    private String fieldNameToKey(String fieldName) {
        return METADATA_KEY_PREFIX + fieldName.replaceAll("_", ".");
    }

    private String localizedToKey(String localizedName) {
        return localizedName.toLowerCase().replaceAll(" ", "");
    }

    private class LookupImpl implements Lookup {
        private Map<Locale, Map<String, String>> fieldToName = new HashMap<Locale, Map<String, String>>();
        private Map<Locale, Map<String, String>> nameToField = new HashMap<Locale, Map<String, String>>();
        private List<String> keys;

        LookupImpl(List<String> keys) {
            this.keys = keys;
        }

        @Override
        public String toLocalizedName(String fieldName, Locale locale) {
            String lookupKey = fieldNameToKey(fieldName);
            String key = getFieldToName(locale).get(lookupKey);
            return key == null ? fieldName : key;
        }

        @Override
        public String toFieldName(String localizedName, Locale locale) {
            String key = getNameToField(locale).get(localizedToKey(localizedName));
            return key == null ? localizedName : key.substring(METADATA_KEY_PREFIX.length()).replaceAll("\\.", "_");
        }

        private Map<String, String> getNameToField(Locale locale) {
            Map<String, String> map = nameToField.get(locale);
            if (map == null) {
                nameToField.put(locale, map = new TreeMap<String, String>());
                for (Map.Entry<String, String> entry : getFieldToName(locale).entrySet()) {
                    String key = localizedToKey(entry.getValue());
                    if (map.containsKey(key)) {
                        throw new RuntimeException(String.format("Value %s --> %s conflicts with %s --> %s", key, entry.getKey(), key, map.get(key)));
                    }
                    map.put(key, entry.getKey());
                }
            }
            return map;
        }

        private Map<String, String> getFieldToName(Locale locale) {
            Map<String, String> map = fieldToName.get(locale);
            if (map == null) {
                fieldToName.put(locale, map = new TreeMap<String, String>());
                for (String key : keys) {
                    String wholeKey = METADATA_KEY_PREFIX + key;
                    map.put(wholeKey, messageSource.getMessage(wholeKey, null, locale));
                }
            }
            return map;
        }

    }
}
