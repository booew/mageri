/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.mageri.core.assemble;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.PercentRangeAssertion;
import com.milaboratory.mageri.core.input.SMig;
import com.milaboratory.mageri.core.input.index.Read;
import com.milaboratory.mageri.generators.MigWithMutations;
import com.milaboratory.mageri.generators.MutationGenerator;
import com.milaboratory.mageri.generators.RandomMigGenerator;
import com.milaboratory.mageri.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblerMinorTest {
    @Test
    @Category(FastTests.class)
    public void exactTest() {
        System.out.println("Exact consensus assembly test");

        // Prepare reads with minors (bad and good quality), majors and N's
        String
                //                                                    M(C)                        M(bad qual)
                //            N        N                              |                   N       |      N        NN
                //                            m(A,T)       m(bad qual)m(G)                        m(bad qual)   m(C)
                //      0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999
                //      0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
                seq1 = "AGCTGTCATCCACAGTTGACAGCCAGCTCTGCATTAAATACTCTACGGCGGAGAAGCTGGTCGCGAACAGAGAGATGCCGAGTCGCAAGCCNCGCAGGCT",
                seq2 = "AGCTGTNATCCACAGTTGACAGACAGCTCTGCATTAAATACTCTACCGCGGAGAAGCTGGTCGCGANCAGAGAGATGCCGAGTCGCAACCCTCGCAGGCT",
                seq3 = "AGCTGTCATCCACAGTTGACAGTCAGCTCTGCATTAAATACTCTACCGCGGAGAAGCTGGTCGCGAACAGAGAGATGCCGANTCGCAAGCCTCGCAGGCT",
                seq4 = "AGCTGTCATCCACAGNTGACAGCCAGCTCTGCATTTAATACTCTACCGCGGAGAAGCTGGTCGCGAACAGAGAGATGCCGAGTCGCAAGCCTCGCAGGCT",
                seq5 = "AGCTGTCATCCACAGTTGACAGCCAGCTCTGCATTAAATACTCTACCGCGGAGAAGCTGGTCGCGAACAGAGAGTTGCCGAGTCGCAAGCNTCGCAGGCT",
                qua1 = "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII5IIIIIIIIIIIIIIII#IIIIIIII",
                qua2 = "IIIIII#IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII#IIIIIII5IIIIIIIIIIIIIIIIIIIIIIIII",
                qua3 = "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII5IIIIII#IIIIIIIIIIIIIIIIII",
                qua4 = "IIIIIIIIIIIIIII#IIIIIIIIIIIIIIIIIII5IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII5IIIIIIIIIIIIIIIIIIIIIIIII",
                qua5 = "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII5IIIIIIIIIIIIIII#IIIIIIIII",
                cons = "AGCTGTCATCCACAGTTGACAGCCAGCTCTGCATTAAATACTCTACCGCGGAGAAGCTGGTCGCGAACAGAGAGATGCCGAGTCGCAAGCCTCGCAGGCT";

        // Correct minors
        Set<Integer> minors = new HashSet<>();

        minors.add(Mutations.createSubstitution(22, NucleotideAlphabet.INSTANCE.codeFromSymbol(cons.charAt(22)),
                NucleotideAlphabet.INSTANCE.codeFromSymbol('A')));
        minors.add(Mutations.createSubstitution(22, NucleotideAlphabet.INSTANCE.codeFromSymbol(cons.charAt(22)),
                NucleotideAlphabet.INSTANCE.codeFromSymbol('T')));
        minors.add(Mutations.createSubstitution(46, NucleotideAlphabet.INSTANCE.codeFromSymbol(cons.charAt(46)),
                NucleotideAlphabet.INSTANCE.codeFromSymbol('G')));
        minors.add(Mutations.createSubstitution(88, NucleotideAlphabet.INSTANCE.codeFromSymbol(cons.charAt(88)),
                NucleotideAlphabet.INSTANCE.codeFromSymbol('C')));


        // Some noise
        seq1 = "AAA" + seq1;
        qua1 = "###" + qua1;
        seq2 = seq2.substring(0, cons.length() - 3);
        qua2 = qua2.substring(0, cons.length() - 3);
        seq3 = "AA" + seq3;
        qua3 = "##" + qua3;
        seq4 = seq4.substring(2);
        qua4 = qua4.substring(2);

        // Convert to reads (set quality mask)

        List<Read> reads = new ArrayList<>();

        reads.add(new Read(new NucleotideSQPair(seq1, qua1)));
        reads.add(new Read(new NucleotideSQPair(seq2, qua2)));
        reads.add(new Read(new NucleotideSQPair(seq3, qua3)));
        reads.add(new Read(new NucleotideSQPair(seq4, qua4)));
        reads.add(new Read(new NucleotideSQPair(seq5, qua5)));

        // Assemble MIG

        SMig mig = new SMig(null, new NucleotideSequence("ATGC"), reads);

        SAssembler assembler = new SAssembler();
        SConsensus consensus = assembler.assemble(mig);

        Assert.assertEquals("Incorrect consensus sequence", cons, consensus.getConsensusSQPair().getSequence().toString());

        System.out.println("Expected minors:");
        for (int minor : minors) {
            System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, minor));
        }
        System.out.println("Observed minors:");
        for (int minor : consensus.getMinors()) {
            System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, minor));
        }

        for (int minor : minors) {
            Assert.assertTrue("Minor " + Mutations.toString(NucleotideAlphabet.INSTANCE, minor) + " missed",
                    consensus.getMinors().contains(minor));
        }
        for (int minor : consensus.getMinors()) {
            Assert.assertTrue("Minor " + Mutations.toString(NucleotideAlphabet.INSTANCE, minor) + " is erroneous",
                    minors.contains(minor));
        }
    }

    @Test
    @Category(FastTests.class)
    public void minorTest() {
        minorTest(true,
                PercentRangeAssertion.createLowerBound("Specificity", "No indel minor recovery", 95),
                PercentRangeAssertion.createLowerBound("Sensitivity", "No indel minor recovery", 95));

        System.out.println("[Indel-compatible assembler not implemented yet: no read dropping in default assembler]");
        minorTest(false,
                PercentRangeAssertion.createDummy("Specificity", "Minor recovery"),
                PercentRangeAssertion.createDummy("Sensitivity", "Minor recovery"));
    }

    public void minorTest(boolean noIndel,
                          PercentRangeAssertion specificity, PercentRangeAssertion sensitivity) {
        int nRepetitions = 1000;
        RandomMigGenerator migGenerator = new RandomMigGenerator();
        if (noIndel) {
            migGenerator.setMutationGenerator(MutationGenerator.NO_INDEL);
        }
        migGenerator.setMaxRandomFlankSize(10);
        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        SAssembler assembler = new SAssembler();
        SConsensus consensus;

        int minorsTN = 0, minorsTP = 0, minorsFP = 0, minorsFN = 0;
        for (int i = 0; i < nRepetitions; i++) {
            NucleotideSequence core = referenceGenerator.nextSequence();
            MigWithMutations randomMig = migGenerator.nextMig(core);

            consensus = assembler.assemble(randomMig.getSMig());

            if (consensus != null) {
                Set<Integer> minors = new HashSet<>();

                for (int minor : randomMig.getMinorMutationCounts().keySet()) {
                    if (Mutations.isSubstitution(minor)) {
                        minors.add(minor);
                    }
                }

                int minorsExpectedCount = minors.size(),
                        minorsObservedCount = consensus.getMinors().size(),
                        minorsPossibleCount = consensus.getConsensusSQPair().size() * 3;

                minors.retainAll(consensus.getMinors());
                int overlap = minors.size();

                minorsTP += overlap;
                minorsFP += minorsObservedCount - overlap;
                minorsFN += minorsExpectedCount - overlap;
                minorsTN += minorsPossibleCount - minorsExpectedCount - minorsObservedCount + overlap;
            }
        }

        specificity.assertInRange(minorsTN, minorsFP + minorsTN);
        sensitivity.assertInRange(minorsTP, minorsTP + minorsFN);
    }
}