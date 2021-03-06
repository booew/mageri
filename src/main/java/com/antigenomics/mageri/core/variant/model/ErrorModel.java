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

package com.antigenomics.mageri.core.variant.model;

import com.antigenomics.mageri.core.mutations.Mutation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ErrorModel extends Serializable {
    ErrorRateEstimate computeErrorRate(Mutation mutation);

    ErrorRateEstimate computeErrorRate(int pos, int from, int to);

    VariantQuality computeQuality(int majorCount, int coverage, Mutation mutation);

    VariantQuality computeQuality(int majorCount, int coverage, int pos, int from, int to);
}
