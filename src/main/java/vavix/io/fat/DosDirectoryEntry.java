/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.DataInput;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

import vavi.util.Debug;
import vavi.util.win32.DateUtil;


/**
 * DosDirectoryEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public class DosDirectoryEntry implements FileEntry {
    /** */
    private static final long serialVersionUID = 1003655836319404523L;

    /** */
    String filename;
    /** */
    int attribute;
    /** */
    int capitalFlag;
    /** unit is 10ms */
    Date created;
    /** unit is date */
    Date lastAccessed;
    /** unit is 2sec */
    Date lastModified;
    /** */
    int startCluster;
    /** */
    long length;

    /** */
    protected String getPrefixString() {
        return "";
    }

    /** */
    DosDirectoryEntry(DataInput leis) throws IOException {
        byte[] b1 = new byte[11];
        leis.readFully(b1);
        filename = getPrefixString() + new String(b1, getPrefixString().length(), 8 - getPrefixString().length()).trim();
        String extention = new String(b1, 8, 3).trim();
        filename += extention.length() > 0 ? '.' + extention : "";
        attribute = leis.readUnsignedByte();
        capitalFlag = leis.readUnsignedByte();
        int lastCreated10msec = leis.readUnsignedByte();
        int lastCreatedTimeDos = leis.readShort();
        int lastCreatedDateDos = leis.readShort();
        created = new Date(DateUtil.dosDateTimeToLong(lastCreatedDateDos, lastCreatedTimeDos) + lastCreated10msec * 10);
        int lastAccessedDateDos = leis.readShort();
        lastAccessed = new Date(DateUtil.dosDateTimeToLong(lastAccessedDateDos, 0));
        int startClusterHigh = leis.readShort();
        int lastModifiedTimeDos = leis.readShort();
        int lastModifiedDateDos = leis.readShort();
        lastModified = new Date(DateUtil.dosDateTimeToLong(lastModifiedDateDos, lastModifiedTimeDos));
        int startClusterLow = leis.readShort() & 0xffff;
        startCluster = (startClusterHigh << 16) | startClusterLow; 
        length = leis.readInt();
    }

    /** */
    public boolean isDirectory() {
        return (attribute & 0x10) != 0;
    }

    /** */
    String longName;

    /** */
    public final void setLongName(Collection<LongNameDirectoryEntry> longNames) {
        StringBuilder sb = new StringBuilder();
        for (LongNameDirectoryEntry entry : longNames) {
Debug.println(Level.FINE, "subEntryNo: " + entry.subEntryNo + ", " + entry.isLast + ", " + entry.filename);
            sb.append(entry.filename);
        }
        longName = sb.toString();
Debug.println(Level.FINE, "longName: " + longName + ", " + longNames.size() + ", " + filename);
    }

    /** */
    public String getName() {
        if (longName != null) {
            return longName;
        } else {
            return filename;
        }
    }

    /** */
    public int getStartCluster() {
        return startCluster;
    }

    /** */
    public int compareTo(FileEntry entry) {
        return getName().compareTo(entry.getName());
    }

    /** */
    public long length() {
        return length;
    }

    /** */
    public long lastModified() {
        return lastModified.getTime();
    }

    /** */
    public long lastAccessed() {
        return lastAccessed.getTime();
    }

    /** */
    public long created() {
        return created.getTime();
    }

    @Override
    public String toString() {
        return getName() + ", " + length() + ", " + LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified()), ZoneId.of("+9"));
    }
}

/* */
