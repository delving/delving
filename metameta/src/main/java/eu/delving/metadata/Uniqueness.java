/*
 * Copyright 2010 DELVING BV
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

import java.util.BitSet;

/**
 * Use some adjacent primes to check for uniqueness of strings based on hashCode
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Uniqueness {
    private static final int[] PRIMES = {
            4105001, 4105019, 4105033, 4105069,
            4105091, 4105093, 4105103, 4105111,
            4105151, 4105169, 4105181, 4105183,
    };
    private BitSet[] bitSet = new BitSet[PRIMES.length];

    public Uniqueness() {
        for (int walk = 0; walk < bitSet.length; walk++) {
            bitSet[walk] = new BitSet(PRIMES[walk]);
        }
    }

    public boolean isRepeated(String text) {
        int hashCode = text.hashCode();
        boolean setEverywhere = true;
        for (int walk = 0; walk < bitSet.length && setEverywhere; walk++) {
            if (!getBit(hashCode, walk)) {
                setEverywhere = false;
            }
        }
        if (setEverywhere) {
            return true;
        }
        for (int walk = 0; walk < bitSet.length; walk++) {
            setBit(hashCode, walk);
        }
        return false;
    }

    private boolean getBit(int hashCode, int bitSetIndex) {
        int offset = PRIMES[(bitSetIndex + 1) % PRIMES.length];
        int bitNumber = Math.abs((hashCode + offset) % bitSet[bitSetIndex].size());
        return bitSet[bitSetIndex].get(bitNumber);
    }

    private void setBit(int hashCode, int bitSetIndex) {
        int offset = PRIMES[(bitSetIndex + 1) % PRIMES.length];
        int bitNumber = Math.abs((hashCode + offset) % bitSet[bitSetIndex].size());
        bitSet[bitSetIndex].set(bitNumber);
    }
}