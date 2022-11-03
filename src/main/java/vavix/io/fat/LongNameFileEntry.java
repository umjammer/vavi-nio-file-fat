/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import vavi.util.Debug;


/**
 * LongNameFileEntry.
 * <p>
 * LNF (Long Name FileEntry) consists multiple LNF entries indexed by {@link #subEntryNo}.
 * and this library assumed LNF entries are located just before SNF. means
 * <pre>
 *   LNF for 'foo...' no.1
 *   LNF for 'foo...' no.2
 *                    :
 *   LNF for 'foo...' last
 *   SNF for 'foo...'
 * </pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
class LongNameFileEntry implements Comparable<LongNameFileEntry> {

    /** */
    private static final long serialVersionUID = 1640728749170150017L;

    /** */
    private static final int LAST_LONG_ENTRY = 0x40;

    /** LDIR_Ord: 1 ~ 20 */
    int subEntryNo;
    /** */
    boolean isLast;
    /** LDIR_Attr: should be ATTR_LONG_NAME (ATTR_READ_ONLY | ATTR_HIDDEN | ATTR_SYSTEM | ATTR_VOLUME_ID) */
    int attribute;
    /** LDIR_Name1: 0 ~ 4 , LDIR_Name2: 5 ~ 10, LDIR_Name3: 11 ~ 12 */
    String filename;
    /** LDIR_Chksum: */
    int shortNameCheckSum;

    /** */
    LongNameFileEntry(DataInput leis) throws IOException {
        int sequenceByte = leis.readUnsignedByte(); // LDIR_Ord
        subEntryNo = sequenceByte & 0x3f;
        isLast = (sequenceByte & LAST_LONG_ENTRY) != 0;
        byte[] b1 = new byte[10];
        leis.readFully(b1); // LDIR_Name1
        filename = new String(b1, 0, 10, StandardCharsets.UTF_16LE);
        attribute = leis.readUnsignedByte(); // LDIR_Attr
        int b = leis.readUnsignedByte(); // LDIR_Type should be 0
        shortNameCheckSum = leis.readUnsignedByte(); // LDIR_Chksum
        byte[] b2 = new byte[12];
        leis.readFully(b2); // LDIR_Name2
        filename += new String(b2, 0, 12, StandardCharsets.UTF_16LE);
        leis.readShort(); // LDIR_FstClusLO always 0
        byte[] b4 = new byte[4];
        leis.readFully(b4); // LDIR_Name3
        filename += new String(b4, 0, 4, StandardCharsets.UTF_16LE);
        int p = filename.indexOf(0);
        if (p != -1) {
            filename = filename.substring(0, p);
        }
Debug.println(Level.FINE, "subEntryNo: " + subEntryNo + ", " + isLast + ", " + filename);
    }

    @Override
    public int compareTo(LongNameFileEntry entry) {
        return this.subEntryNo - entry.subEntryNo;
    }

    /** */
    private byte checksum() {
        byte sum;
        for (int i = sum = 0; i < 11; i++) {
            sum = (byte) ((sum >> 1) + (sum << 7) + filename.charAt(i));
        }
        return sum;
    }
}

/* */
