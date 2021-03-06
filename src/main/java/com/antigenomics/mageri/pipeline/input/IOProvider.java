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


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class IOProvider {
    protected final String protocol;

    public IOProvider(String protocol) {
        this.protocol = protocol;
    }

    public abstract InputStream getStream(String path) throws IOException;

    public InputStreamWrapper getWrappedStream(String path) throws IOException {
        return new InputStreamWrapper(protocol, path, getStream(path));
    }

    public List<String> readLines(String path) throws IOException {
        InputStream inputStream = getStream(path);
        return IOUtils.readLines(inputStream);
    }

    public String read(String path) throws IOException {
        return StringUtils.join(readLines(path), '\n');
    }
}
