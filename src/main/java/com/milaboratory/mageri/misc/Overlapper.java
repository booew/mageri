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
package com.milaboratory.mageri.misc;

import com.milaboratory.core.sequence.NucleotideSQPair;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Overlapper implements Serializable {
    private final int overlapSeedSize, maxConsMms, minOverlapSize;
    private final double maxOverlapMismatchRatio;
    private AtomicLong overlappedCount = new AtomicLong(),
            readThroughCount = new AtomicLong(),
            totalCount = new AtomicLong();

    public Overlapper(int minOverlapSize,
                      int overlapSeedSize, int maxConsMms,
                      double maxOverlapMismatchRatio) {
        this.minOverlapSize = minOverlapSize;
        this.overlapSeedSize = overlapSeedSize;
        this.maxConsMms = maxConsMms;
        this.maxOverlapMismatchRatio = maxOverlapMismatchRatio;
    }

    public Overlapper() {
        this(10, 5, 2, 0.05); // for default checkout output
    }

    public long getOverlappedCount() {
        return overlappedCount.get();
    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public long getReadThroughCount() {
        return readThroughCount.get();
    }

    public double getOverlapEfficiency() {
        return overlappedCount.get() / (double) totalCount.get();
    }

    public double getReadthroughRate() {
        return readThroughCount.get() / (double) overlappedCount.get();
    }

    public OverlapResult overlap(NucleotideSQPair sqPair1, NucleotideSQPair sqPair2) {
        String seq1 = sqPair1.getSequence().toString(),
                seq2 = sqPair2.getSequence().toString(),
                qual1 = sqPair1.getQuality().toString(),
                qual2 = sqPair2.getQuality().toString();

        totalCount.incrementAndGet();

        int maxOffset = seq2.length() - overlapSeedSize + 1;

        for (int y = 0; y < maxOffset; y++) {
            String kmer = seq2.substring(y, y + overlapSeedSize);
            Pattern pattern = Pattern.compile(kmer);
            Matcher matcher = pattern.matcher(seq1);

            while (matcher.find()) {
                int x = matcher.start();

                if (x >= 0) {
                    int delta = x - y;

                    boolean readThrough = delta < 0;

                    NucleotideSQPair overlappedSqPair = readThrough ? overlap(-delta, seq2, seq1, qual2, qual1) :
                            overlap(delta, seq1, seq2, qual1, qual2);

                    if (overlappedSqPair != null) {
                        if (readThrough) {
                            readThroughCount.incrementAndGet();
                        }
                        
                        overlappedCount.incrementAndGet();
                        
                        return new OverlapResult(
                                readThrough,
                                overlappedSqPair,
                                delta);
                    }
                }
            }
        }

        return new OverlapResult(false, null, 0);
    }

    private NucleotideSQPair overlap(int delta,
                                     String seq1, String seq2,
                                     String qual1, String qual2) {
        StringBuilder sbSeq = new StringBuilder(), sbQual = new StringBuilder();

        // - skipped
        // = as is
        // ~ best taken
        // * seed (just for reference)
        // ? existing is taken
        //          
        //                              p3
        //       0 ~~~~~~y***~~~~~~~~~??o2?? l2  seq2, j=0..l2
        //  0 =====~~~~~~x***~~~~~~~~~??o1?? l1  seq1, i=0..l1
        //    p1  |        p2
        //        delta

        int nMms = 0, nConsMms = 0;

        boolean overhang1 = seq2.length() < seq1.length() - delta;

        int size = overhang1 ? seq2.length() : (seq1.length() - delta);

        if (size < minOverlapSize) {
            return null;
        }

        for (int i2 = 0; i2 < size; i2++) {
            int i1 = delta + i2;
            char c1 = seq1.charAt(i1), c2 = seq2.charAt(i2);
            byte q1 = (byte) qual1.charAt(i1), q2 = (byte) qual2.charAt(i2);
            if (c1 != c2) {
                if ((++nMms / (double) size) > maxOverlapMismatchRatio ||
                        ++nConsMms > maxConsMms) {
                    return null;
                }
                if (q1 > q2) {
                    sbSeq.append(c1);
                    sbQual.append((char) (Math.max(q1 - q2, 3) + 33));
                } else {
                    sbSeq.append(c2);
                    sbQual.append((char) (Math.max(q2 - q1, 3) + 33));
                }
            } else {
                sbSeq.append(c1);
                sbQual.append((char) (Math.min(q1 + q2, 42) + 33));
            }
        }

        return overhang1 ?
                new NucleotideSQPair(
                        seq1.substring(0, delta) + sbSeq.toString() + seq1.substring(delta + size),
                        qual1.substring(0, delta) + sbQual.toString() + qual1.substring(delta + size)) :
                new NucleotideSQPair(
                        seq1.substring(0, delta) + sbSeq.toString() + seq2.substring(size),
                        qual1.substring(0, delta) + sbQual.toString() + qual2.substring(size));
    }

    public class OverlapResult {
        private final NucleotideSQPair sqPair;
        private final boolean readThrough;
        private final int delta;

        public OverlapResult(boolean readThrough,
                             NucleotideSQPair sqPair,
                             int delta) {
            this.readThrough = readThrough;
            this.sqPair = sqPair;
            this.delta = delta;
        }

        public boolean readThrough() {
            return readThrough;
        }

        public boolean overlapped() {
            return sqPair != null;
        }

        public NucleotideSQPair getSQPair() {
            return sqPair;
        }

        public int getOffset1() {
            return readThrough ? -delta : 0;
        }

        public int getOffset2() {
            return readThrough ? 0 : delta;
        }
    }
}
