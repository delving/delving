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

import org.springframework.context.MessageSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MultilingualAccessTranslator {
    private static final String METADATA_KEY_PREFIX = "_metadata.";
    private MessageSource messageSource;
    private Set<String> metadataKeys = new TreeSet<String>();
    private Map<Locale, Map<String, String>> fieldToName = new HashMap<Locale, Map<String, String>>();
    private Map<Locale, Map<String, String>> nameToField = new HashMap<Locale, Map<String, String>>();

    public void setExposedKeysMessageSource(ExposedKeysMessageSource exposedKeysMessageSource) {
        this.messageSource = exposedKeysMessageSource;
        for (String key : exposedKeysMessageSource.getKeySet()) {
            if (key.startsWith(METADATA_KEY_PREFIX)) {
                metadataKeys.add(key);
            }
        }
    }

    public String toLocalizedName(String fieldName, Locale locale) {
        return getFieldToName(locale).get(fieldNameToKey(fieldName));
    }

    public String toFieldName(String localizedName, Locale locale) {
        String searchName = localizedToKey(localizedName);
        String key = getNameToField(locale).get(searchName);
        if (key == null) {
            return "unknown";
        }
        return key.substring(METADATA_KEY_PREFIX.length()).replaceAll("\\.", "_");
    }

    private String fieldNameToKey(String fieldName) {
        return METADATA_KEY_PREFIX + fieldName.replaceAll("_", ".");
    }

    private String localizedToKey(String localizedName) {
        return localizedName.toLowerCase().replaceAll(" ", "");
    }

    private Map<String, String> getNameToField(Locale locale) {
        Map<String, String> map = nameToField.get(locale);
        if (map == null) {
            nameToField.put(locale, map = new TreeMap<String, String>());
            for (Map.Entry<String, String> entry : getFieldToName(locale).entrySet()) {
                map.put(localizedToKey(entry.getValue()), entry.getKey());
            }
        }
        return map;
    }

    private Map<String, String> getFieldToName(Locale locale) {
        Map<String, String> map = fieldToName.get(locale);
        if (map == null) {
            fieldToName.put(locale, map = new TreeMap<String, String>());
            for (String key : metadataKeys) {
                map.put(key, messageSource.getMessage(key, null, locale));
            }
        }
        return map;
    }
}
