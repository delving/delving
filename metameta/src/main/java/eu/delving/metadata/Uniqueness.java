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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * Use some adjacent primes to check for uniqueness of strings based on hashCode
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Uniqueness {
    private static final int[] PRIMES = {
            99000007, 99000023, 99000029, 99000037, 99000047,
            99000059, 99000079, 99000101, 99000107, 99000137,
            99000151, 99000157, 99000217, 99000221, 99000259
    };
    private MessageDigest messageDigest;
    private BitSet[] bitSet = new BitSet[PRIMES.length];

    public Uniqueness() {
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available??");
        }
        for (int walk = 0; walk < bitSet.length; walk++) {
            bitSet[walk] = new BitSet(PRIMES[walk]);
        }
    }

    public boolean isRepeated(String text) {
        BigInteger hashCode = hash(text);
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

    private boolean getBit(BigInteger hashCode, int bitSetIndex) {
        BigInteger bitNumber = hashCode.mod(BigInteger.valueOf(bitSet[bitSetIndex].size()));
        return bitSet[bitSetIndex].get(bitNumber.intValue());
    }

    private void setBit(BigInteger hashCode, int bitSetIndex) {
        BigInteger bitNumber = hashCode.mod(BigInteger.valueOf(bitSet[bitSetIndex].size()));
        bitSet[bitSetIndex].set(bitNumber.intValue());
    }

    public BigInteger hash(String string) {
        try {
            messageDigest.reset();
            return new BigInteger(messageDigest.digest(string.getBytes("UTF-8")));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}