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
 * Last modified on 12.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.input;

public class SubMultiplexRule extends UmiRule{
    private final String multiplexId;
    private final String multiplexBarcode;

    public SubMultiplexRule(UmiLocation umiLocation, String umiString,
                            String multiplexId, String multiplexBarcode) {
        super(umiLocation, umiString);
        this.multiplexId = multiplexId;
        this.multiplexBarcode = multiplexBarcode;
    }

    public String getMultiplexId() {
        return multiplexId;
    }

    public String getMultiplexBarcode() {
        return multiplexBarcode;
    }
}
