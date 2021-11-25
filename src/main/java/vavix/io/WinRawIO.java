/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;


/**
 * RawIO. 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060108 nsano initial version <br>
 */
public class WinRawIO implements IOSource {

    /** */
    public WinRawIO(String deviceName) throws IOException {
        deviceName = deviceName.toUpperCase();
        if (!deviceName.matches("[A-Z]\\:")) {
            throw new IllegalArgumentException(deviceName);
        }
        open("\\\\.\\" + deviceName);
    }

    @Override
    public int readSector(byte[] buffer, int sectorNo) throws IOException {
        read(sectorNo, buffer);
        return bytesPerSector;
    }

    @Override
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    //---- native access ----

    /** set by jni */
    private int handle;

    /** set by jni */
    private int bytesPerSector;

    /**
     * @after {@link #handle} and {@link #bytesPerSector} will be set 
     */
    private native void open(String deviceName) throws IOException;

    /**
     * @before {@link #open(int)} was called
     */
    private native void read(int sectorNo, byte[] buffer) throws IOException;

    /**
     * @before {@link #open(int)} was called
     */
    private native void close() throws IOException;

    /** */
    static {
        System.loadLibrary("RawIO");
    }
}

/* */
