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

package com.antigenomics.mageri.pipeline.input;

import com.antigenomics.mageri.preprocessing.CheckoutProcessor;
import com.antigenomics.mageri.preprocessing.PPositionalExtractor;
import com.antigenomics.mageri.preprocessing.SPositionalExtractor;

import java.util.ArrayList;
import java.util.List;

public class PositionalRule extends CheckoutRule {
    private final String sampleName, mask1, mask2;
    private final boolean paired;

    public PositionalRule(String sampleName,
                          String mask1, String mask2,
                          boolean paired) {
        this.sampleName = sampleName;
        this.mask1 = mask1;
        this.mask2 = mask2;
        this.paired = paired;
    }

    @Override
    public CheckoutProcessor getProcessor() {
        return mask2 == null ?
                (paired ?
                        new PPositionalExtractor(sampleName, mask1) :
                        new SPositionalExtractor(sampleName, mask1)) :
                (paired ?
                        new PPositionalExtractor(sampleName, mask1, mask2) :
                        new SPositionalExtractor(sampleName, mask1));
    }

    @Override
    public List<String> getSampleNames() {
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        return sampleNames;
    }

    @Override
    public boolean hasSubMultiplexing() {
        return false;
    }

    @Override
    public String toString() {
        return "preprocessed_rule\n-submultiplex:" + hasSubMultiplexing() + "\n-samples:" + sampleName;
    }
}
