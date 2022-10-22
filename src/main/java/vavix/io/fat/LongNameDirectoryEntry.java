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
 * LongNameDirectoryEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
class LongNameDirectoryEntry implements DirectoryEntry, Comparable<LongNameDirectoryEntry> {
    /** */
    private static final long serialVersionUID = 1640728749170150017L;

    /** */
    int subEntryNo;
    /** */
    boolean isLast;
    /** */
    int attribute;
    /** */
    String filename;
    /** */
    int shortNameCheckSum;

    /** */
    LongNameDirectoryEntry(DataInput leis) throws IOException {
        int sequenceByte = leis.readUnsignedByte();
        subEntryNo = sequenceByte & 0x3f;
        isLast = (sequenceByte & 0x40) != 0;
        byte[] b1 = new byte[10];
        leis.readFully(b1);
        filename = new String(b1, 0, 10, StandardCharsets.UTF_16LE);
        attribute = leis.readUnsignedByte(); 
        leis.readUnsignedByte(); // longEntryType
        shortNameCheckSum = leis.readUnsignedByte();
        byte[] b2 = new byte[12];
        leis.readFully(b2);
        filename += new String(b2, 0, 12, StandardCharsets.UTF_16LE);
        byte[] b3 = new byte[2];
        leis.readFully(b3);
        byte[] b4 = new byte[4];
        leis.readFully(b4);
        filename += new String(b4, 0, 4, StandardCharsets.UTF_16LE);
        int p = filename.indexOf(0);
        if (p != -1) {
            filename = filename.substring(0, p);
        }
Debug.println(Level.FINE, "subEntryNo: " + subEntryNo + ", " + isLast + ", " + filename);
    }

    @Override
    public int compareTo(LongNameDirectoryEntry entry) {
        return this.subEntryNo - entry.subEntryNo;
    }
}

/* */
