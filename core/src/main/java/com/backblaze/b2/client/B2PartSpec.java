/*
 * Copyright 2017, Backblaze Inc. All Rights Reserved.
 * License https://www.backblaze.com/using_b2_code.html
 */
package com.backblaze.b2.client;

import java.util.Comparator;
import java.util.Objects;

/**
 * A B2PartSpec represents part of a large file.
 * It has the partNumber and the offset &amp; length of the part in the file.
 */
public class B2PartSpec implements Comparable<B2PartSpec> {
/*    private static Comparator<B2PartSpec> comparator = Comparator
            .comparingInt(B2PartSpec::getPartNumber)
            .thenComparing(Comparator.comparingLong(B2PartSpec::getStart))
            .thenComparing(Comparator.comparingLong(B2PartSpec::getLength)); */

    private static Comparator<B2PartSpec> comparator = new Comparator<B2PartSpec>(){

        @Override
        public int compare(B2PartSpec p1, B2PartSpec p2) {
            int partComparison = partNumberComparator.compare(p1, p2);
            if( partComparison != 0 ){
                return partComparison;
            } else {
                int startComparison = startComparator.compare(p1, p2);
                if( startComparison != 0){
                    return startComparison;
                } else {
                    return lengthComparator.compare(p1, p2);
                }
            }
        }
    };

    public static Comparator<B2PartSpec> partNumberComparator = new Comparator<B2PartSpec>() {
        @Override
        public int compare(B2PartSpec p1, B2PartSpec p2) {
            return p1.getPartNumber() - p2.getPartNumber();
        }
    };
    public static Comparator<B2PartSpec> startComparator = new Comparator<B2PartSpec>(){

        @Override
        public int compare(B2PartSpec p1, B2PartSpec p2) {
            if( p1.getStart() == p2.getStart())
                return 0;
            else
                return p1.getStart() > p2.getStart() ? 1 : -1;
        }
    };
    public static Comparator<B2PartSpec> lengthComparator = new Comparator<B2PartSpec>(){

        @Override
        public int compare(B2PartSpec p1, B2PartSpec p2) {
            if( p1.getLength() == p2.getLength())
                return 0;
            else
                return p1.getLength() > p2.getLength() ? 1 : -1;
        }
    };

    final int partNumber; // one-based part number.
    final long start;     // byte offset in the file (zero-based)
    final long length;    // length in bytes.

    B2PartSpec(int partNumber,
               long start,
               long length) {

        this.partNumber = partNumber;
        this.start = start;
        this.length = length;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public long getStart() {
        return start;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "B2PartSpec{" +
                "#" + partNumber +
                ", start=" + start +
                ", pastEnd=" + (start+length) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        B2PartSpec partSpec = (B2PartSpec) o;
        return partNumber == partSpec.partNumber &&
                start == partSpec.start &&
                length == partSpec.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(partNumber, start, length);
    }


    @Override
    public int compareTo(B2PartSpec o) {
        return comparator.compare(this, o);
    }
}
