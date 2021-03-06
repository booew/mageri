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
package com.antigenomics.mageri.core.input;

import cc.redberry.pipe.OutputPortCloseable;
import com.antigenomics.mageri.core.input.index.Read;
import com.antigenomics.mageri.core.input.index.ReadInfo;
import com.antigenomics.mageri.pipeline.RuntimeParameters;
import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.CheckoutResult;
import com.antigenomics.mageri.preprocessing.SCheckoutResult;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.quality.QualityFormat;
import com.milaboratory.core.sequencing.io.fastq.SFastqReader;
import com.milaboratory.core.sequencing.read.SequencingRead;
import com.antigenomics.mageri.pipeline.analysis.Sample;
import com.milaboratory.util.CompressionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SMigReader extends MigReader<SMig> {
    public SMigReader(SFastqReader reader,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters,
                      RuntimeParameters runtimeParameters)
            throws IOException, InterruptedException {
        super(preprocessorParameters, checkoutProcessor, runtimeParameters);

        buildUmiIndex(new SingleReaderWrapper(reader));
    }

    public SMigReader(InputStream inputStream1,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters,
                      RuntimeParameters runtimeParameters)
            throws IOException, InterruptedException {
        this(new SFastqReader(inputStream1, QualityFormat.Phred33, CompressionType.None),
                checkoutProcessor,
                preprocessorParameters, runtimeParameters);
    }

    public SMigReader(InputStream inputStream1,
                      CheckoutProcessor checkoutProcessor,
                      PreprocessorParameters preprocessorParameters)
            throws IOException, InterruptedException {
        this(inputStream1,
                checkoutProcessor,
                preprocessorParameters, RuntimeParameters.DEFAULT);
    }

    public SMigReader(InputStream inputStream1,
                      CheckoutProcessor checkoutProcessor)
            throws IOException, InterruptedException {
        this(inputStream1,
                checkoutProcessor,
                PreprocessorParameters.DEFAULT);
    }

    @Override
    protected synchronized SMig take(Sample sample, String barcodeName, int sizeThreshold) {
        Iterator<Map.Entry<NucleotideSequence, List<ReadInfo>>> iterator = iteratorMap.get(barcodeName);
        while (iterator.hasNext()) {
            Map.Entry<NucleotideSequence, List<ReadInfo>> entry = iterator.next();
            if (entry.getValue().size() >= sizeThreshold && !checkUmiMismatch(barcodeName, entry.getKey())) {
                List<Read> readList = new LinkedList<>();

                for (ReadInfo readInfo : entry.getValue()) {
                    readList.add(groom(readInfo.getReadContainer().getFirst(),
                            readInfo.getCheckoutResult(),
                            preprocessorParameters.trimAdapters()));
                }

                return new SMig(sample, entry.getKey(), readList);
            }
        }
        return null;
    }

    public static Read groom(Read read, CheckoutResult checkoutResult, boolean trimAdapters) {
        if (trimAdapters && checkoutResult instanceof SCheckoutResult) {
            return read.trim5Prime(checkoutResult.getMasterResult().getTo());
        }
        // NOTE: Otherwise the checkout processor is a HeaderExtractor
        // For single-end preprocessed data, we have a convention that
        // a) read header contains UMI sequence (UMI:seq:qual)
        // b) reads are oriented in correct direction
        // c) adapter/primer sequences are trimmed
        return read;
    }

    @Override
    public boolean isPairedEnd() {
        return false;
    }

    private class SingleReaderWrapper implements OutputPortCloseable<SequencingRead> {
        private final SFastqReader reader;

        public SingleReaderWrapper(SFastqReader reader) {
            this.reader = reader;
        }

        @Override
        public void close() {
            reader.close();
        }

        @Override
        public SequencingRead take() {
            // allows working with disabled buffering
            synchronized (reader) {
                return reader.take();
            }
        }
    }
}
