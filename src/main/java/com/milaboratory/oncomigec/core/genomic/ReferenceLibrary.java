/*
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
package com.milaboratory.oncomigec.core.genomic;

import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.oncomigec.util.Util;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ReferenceLibrary {
    private final List<Reference> references = new ArrayList<>();
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final Set<NucleotideSequence> referenceSequences = new HashSet<>();
    private int globalId = 0;

    public static ReferenceLibrary fromInput(InputStream input) {
        throw new NotImplementedException();
    }

    public ReferenceLibrary() {

    }

    public ReferenceLibrary(File referenceFile) throws IOException {
        this(Util.readFasta(referenceFile));
    }

    public ReferenceLibrary(Collection<SSequencingRead> fastaRecords) {
        for (SSequencingRead record : fastaRecords) {
            NucleotideSequence sequence = record.getData().getSequence();
            String[] descriptionFields = record.getDescription().split("[ \t]");
            addReferenceAndRC(descriptionFields[0],
                    sequence);
        }
    }

    synchronized void addReference(String name, NucleotideSequence sequence, boolean rc) {
        sequence = rc ? sequence.getReverseComplement() : sequence;
        Reference reference = new Reference(globalId, name, sequence, rc);
        String fullName = reference.getFullName();

        if (referenceSequences.contains(sequence))
            throw new RuntimeException("Duplicate sequences not allowed in reference library. " +
                    sequence);
        if (nameToId.containsKey(reference.getFullName()))
            throw new RuntimeException("Duplicate sequence names (with respect to reverse complement flag, _RC) " +
                    "are not allowed. " + reference.getFullName());

        referenceSequences.add(sequence);
        nameToId.put(fullName, globalId);
        references.add(reference);

        globalId++;
    }

    public void addReference(String name, NucleotideSequence sequence) {
        addReference(name, sequence, false);
    }

    public void addReferenceAndRC(String name, NucleotideSequence sequence) {
        addReference(name, sequence, false);
        addReference(name, sequence, true);
    }

    public Reference getByGlobalId(int globalId) {
        if (globalId < 0 || globalId >= references.size())
            throw new IndexOutOfBoundsException();
        return references.get(globalId);
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }
}