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

import eu.europeana.sip.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.definitions.annotations.EuropeanaField;
import eu.europeana.sip.definitions.annotations.FieldCategory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Validate a record
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordValidator {
    private static final int [] PRIMES = {
            4105001,      4105019,      4105033,      4105069,
            4105091,      4105093,      4105103,      4105111,
            4105151,      4105169,      4105181,      4105183,
    };
    private Map<String, EuropeanaField> fieldMap = new HashMap<String, EuropeanaField>();
    private BitSet [] bitSet;
    private boolean checkUniqueness;

    public RecordValidator(AnnotationProcessor annotationProcessor, boolean checkUniqueness) {
        this.checkUniqueness = checkUniqueness;

        if (checkUniqueness) {
            bitSet = new BitSet[PRIMES.length];
            for (int walk=0; walk<bitSet.length; walk++) {
                bitSet[walk] = new BitSet(PRIMES[walk]);
            }
        }
        for (EuropeanaField field : annotationProcessor.getAllFields()) {
            if (field.europeana().category() != FieldCategory.INDEX_TIME_ADDITION) {
                fieldMap.put(field.getXmlName(), field);
            }
        }
    }

    public void validate(MetadataRecord metadataRecord, List<FieldEntry> fieldEntries) throws RecordValidationException {
        List<String> problems = new ArrayList<String>();
        validateAgainstAnnotations(fieldEntries, problems);
        if (!problems.isEmpty()) {
            throw new RecordValidationException(metadataRecord, problems);
        }
    }

    private void validateAgainstAnnotations(List<FieldEntry> fieldEntries, List<String> problems) {
        Map<String, Counter> counterMap = new HashMap<String, Counter>();
        for (FieldEntry fieldEntry : fieldEntries) {
            EuropeanaField field = fieldMap.get(fieldEntry.getTag());
            if (field == null) {
                problems.add(String.format("Unknown XML element [%s]", fieldEntry.getTag()));
            }
            else {
                Counter counter = counterMap.get(fieldEntry.getTag());
                if (counter == null) {
                    counter = new Counter();
                    counterMap.put(fieldEntry.getTag(), counter);
                }
                counter.count++;
                Set<String> enumValues = field.getEnumValues();
                if (enumValues != null && !field.europeana().valueMapped() && !enumValues.contains(fieldEntry.getValue())) {
                    StringBuilder enumString = new StringBuilder();
                    Iterator<String> walk = enumValues.iterator();
                    while (walk.hasNext()) {
                        enumString.append(walk.next());
                        if (walk.hasNext()) {
                            enumString.append(',');
                        }
                    }
                    problems.add(String.format("Value for [%s] was [%s] which does not belong to [%s]", fieldEntry.getTag(), fieldEntry.getValue(), enumString.toString()));
                }
//                if (field.europeana().constant() && constantMap != null) {  todo: constant fields can potentially be mapped
//                    String value = constantMap.get(fieldEntry.getTag());
//                    if (value == null) {
//                        constantMap.put(fieldEntry.getTag(), fieldEntry.getValue());
//                    }
//                    else if (!value.equals(fieldEntry.getValue())) {
//                        problems.add(String.format("Value for [%s] should be constant but it had multiple values [%s] and [%s]", fieldEntry.getTag(), fieldEntry.getValue(), value));
//                    }
//                }
                String regex = field.europeana().regularExpression();
                if (!regex.isEmpty()) {
                    if (!fieldEntry.getValue().matches(regex)) {
                        problems.add(String.format("Value for [%s] was [%s] which does not match regular expression [%s]", fieldEntry.getTag(), fieldEntry.getValue(), regex));
                    }
                }
                if (field.europeana().url()) {
                    try {
                        new URL(fieldEntry.getValue());
                    }
                    catch (MalformedURLException e) {
                        problems.add(String.format("URL value for [%s] was [%s] which is malformed", fieldEntry.getTag(), fieldEntry.getValue()));
                    }
                }
                if (field.europeana().id() && checkUniqueness) {
                    checkEntryUniqueness(problems, fieldEntry);
                }
            }
        }
        Map<String, Boolean> present = new TreeMap<String, Boolean>();
        for (EuropeanaField field : fieldMap.values()) {
            if (!field.europeana().requiredGroup().isEmpty()) {
                present.put(field.europeana().requiredGroup(), false);
            }
        }
        for (FieldEntry fieldEntry : fieldEntries) {
            EuropeanaField field = fieldMap.get(fieldEntry.getTag());
            if (field != null && !field.europeana().requiredGroup().isEmpty()) {
                present.put(field.europeana().requiredGroup(), true);
            }
        }
        for (Map.Entry<String,Boolean> entry : present.entrySet()) {
            if (!entry.getValue()) {
                problems.add(String.format("Required field violation for [%s]", entry.getKey()));
            }
        }
        for (EuropeanaField field : fieldMap.values()) {
            Counter counter = counterMap.get(field.getXmlName());
            if (counter != null && !field.solr().multivalued() && counter.count > 1) {
                problems.add(String.format("Single-valued field [%s] had %d values", field.getXmlName(), counter.count));
            }
        }
    }

    private void checkEntryUniqueness(List<String> problems, FieldEntry fieldEntry) {
        int hashCode = fieldEntry.getValue().hashCode();
        boolean setEverywhere = true;
        for (int walk=0; walk<bitSet.length && setEverywhere; walk++) {
            if (!getBit(hashCode, walk)) {
                setEverywhere = false;
            }
        }
        if (setEverywhere) {
            problems.add(String.format("Identifier [%s] must be unique but the value [%s] appears more than once", fieldEntry.getTag(), fieldEntry.getValue()));
        }
        for (int walk=0; walk<bitSet.length && setEverywhere; walk++) {
            setBit(hashCode, walk);
        }
    }

    private boolean getBit(int hashCode, int bitSetIndex) {
        int offset = PRIMES[(bitSetIndex+1) % PRIMES.length];
        int bitNumber = Math.abs((hashCode + offset) % bitSet[bitSetIndex].size());
        return bitSet[bitSetIndex].get(bitNumber);
    }

    private void setBit(int hashCode, int bitSetIndex) {
        int offset = PRIMES[(bitSetIndex+1) % PRIMES.length];
        int bitNumber = Math.abs((hashCode + offset) % bitSet[bitSetIndex].size());
        bitSet[bitSetIndex].set(bitNumber);
    }

    private static class Counter {
        int count;
    }
}