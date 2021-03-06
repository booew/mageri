/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.core.input.index;

import com.antigenomics.mageri.misc.QualityDefaults;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceBuilder;
import com.milaboratory.core.sequence.quality.SequenceQualityPhred;
import com.milaboratory.core.sequence.quality.SequenceQualityUtils;

import java.util.BitSet;
import java.util.Random;

public class MaskedRead implements Read {
    private static final Random rnd = new Random(511022);
    private final NucleotideSequence sequence;
    private final BitSet qualityMask;

    public MaskedRead(NucleotideSequence sequence,
                      BitSet qualityMask) {
        this.sequence = sequence;
        this.qualityMask = qualityMask;
    }

    public MaskedRead(NucleotideSQPair nucleotideSQPair) {
        this(nucleotideSQPair, QualityProvider.DEFAULT);
    }

    public MaskedRead(NucleotideSQPair nucleotideSQPair,
                      QualityProvider qualityProvider) {
        NucleotideSequence sequence = nucleotideSQPair.getSequence();

        SequenceQualityPhred qual = nucleotideSQPair.getQuality();

        // This is required as old milib reader replaces N's with A.
        // It also sets quality to BAD_QUALITY_VALUE, so here we
        // generate a random base at those positions
        NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder(sequence.size());

        for (int i = 0; i < sequence.size(); i++) {
            if (qual.value(i) > SequenceQualityUtils.BAD_QUALITY_VALUE) {
                nsb.setCode(i, sequence.codeAt(i));
            } else {
                nsb.setCode(i, (byte) rnd.nextInt(4));
            }
        }

        this.sequence = nsb.create();
        this.qualityMask = qualityProvider.convert(nucleotideSQPair.getQuality());
    }

    @Override
    public Read rc() {
        BitSet qualityMask = new BitSet(length());
        for (int i = 0; i < length(); i++) {
            qualityMask.set(i, this.qualityMask.get(length() - i - 1));
        }
        return new MaskedRead(sequence.getReverseComplement(), qualityMask);
    }

    @Override
    public Read trim5Prime(int from) {
        return region(from, length());
    }

    @Override
    public Read trim3Prime(int to) {
        return region(0, to);
    }

    @Override
    public Read region(int from, int to) {
        return new MaskedRead(sequence.getRange(from, to),
                qualityMask.get(from, to));
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    @Override
    public NucleotideSQPair toNucleotideSQPair() {
        byte[] qual = new byte[sequence.size()];
        for (int i = 0; i < sequence.size(); i++) {
            if (goodQuality(i)) {
                qual[i] = QualityDefaults.PH33_MAX_QUAL;
            } else {
                qual[i] = QualityDefaults.PH33_MIN_QUAL;
            }
        }
        return new NucleotideSQPair(sequence, new SequenceQualityPhred(qual));
    }

    public boolean goodQuality(int pos) {
        return !qualityMask.get(pos);
    }

    public int length() {
        return sequence.size();
    }
}
