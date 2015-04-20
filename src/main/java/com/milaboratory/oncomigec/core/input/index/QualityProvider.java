/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 10.4.2015 by mikesh
 */

package com.milaboratory.oncomigec.core.input.index;

import com.milaboratory.core.sequence.quality.SequenceQualityPhred;

import java.util.BitSet;

public class QualityProvider {
    private final byte goodQualityThrehsold;

    public QualityProvider(byte goodQualityThrehsold) {
        this.goodQualityThrehsold = goodQualityThrehsold;
    }

    public boolean covert(byte ph33Quality) {
        return ph33Quality >= goodQualityThrehsold;
    }

    public BitSet convert(SequenceQualityPhred sequenceQualityPhred) {
        BitSet qualityString = new BitSet(sequenceQualityPhred.size());
        for (int i = 0; i < sequenceQualityPhred.size(); i++) {
            qualityString.set(i, covert(sequenceQualityPhred.value(i)));
        }
        return qualityString;
    }
}