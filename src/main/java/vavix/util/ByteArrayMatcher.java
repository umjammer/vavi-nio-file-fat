/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util;


/**
 * ByteArrayMatcher.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/03 umjammer initial version <br>
 */
public class ByteArrayMatcher implements Matcher<byte[]> {

    private byte[] source;

    public ByteArrayMatcher(byte[] buffer) {
        this.source = buffer;
    }

    /** Boyer-Moore Algorithm */
    public int indexOf(byte[] pattern, int fromIndex) {
        int[] skipTable = new int[256];
        if (fromIndex >= source.length) {
            return -1;
        }
        for (int ch = 0; ch < skipTable.length; ch++) {
            skipTable[ch] = pattern.length;
        }
        for (int ch = 0; ch < (pattern.length - 1); ch++) {
            skipTable[pattern[ch] & 0xff] = pattern.length - 1 - ch;
        }
        int i = fromIndex + pattern.length - 1;
        while (i < source.length) {
            if (source[i] == pattern[pattern.length - 1]) {
                int j = i;
                int k = pattern.length - 1;
                while (source[--j] == pattern[--k]) {
                    if (k == 0) {
                        return j;
                    }
                }
            }
            i = i + skipTable[source[i] & 0xff];
        }
        return -1;
    }
}

/* */
