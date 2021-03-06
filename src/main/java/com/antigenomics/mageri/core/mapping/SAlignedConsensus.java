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

package com.antigenomics.mageri.core.mapping;

import com.antigenomics.mageri.core.mapping.alignment.AlignmentResult;
import com.antigenomics.mageri.core.mutations.MutationArray;
import com.antigenomics.mageri.pipeline.analysis.Sample;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;

public class SAlignedConsensus extends AlignedConsensus {
    private final MutationArray mutations;
    private final AlignmentResult alignmentResult;
    private final NucleotideSQPair consensusSQPair;

    public SAlignedConsensus(Sample sample, NucleotideSequence umi,
                             NucleotideSQPair consensusSQPair,
                             AlignmentResult alignmentResult,
                             MutationArray mutations) {
        super(sample, umi);
        this.consensusSQPair = consensusSQPair;
        this.mutations = mutations;
        this.alignmentResult = alignmentResult;
    }

    public MutationArray getMutations() {
        return mutations;
    }

    public AlignmentResult getAlignmentResult() {
        return alignmentResult;
    }

    public NucleotideSQPair getConsensusSQPair() {
        return consensusSQPair;
    }

    @Override
    public boolean isMapped() {
        return alignmentResult != null;
    }

    @Override
    public boolean isAligned() {
        return isMapped() && alignmentResult.isGood();
    }

    @Override
    public boolean isChimeric() {
        return false;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }
}
