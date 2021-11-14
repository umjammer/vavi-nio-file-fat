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

    /** */
    protected void finalize() throws Throwable {
        close();
    }

    //---- native access ----

    /** ドライブのハンドル */
    private int handle;

    /** 1 セクタのバイト数 */
    private int bytesPerSector;

    /**
     * @事後条件 {@link #handle} と {@link #bytesPerSector} が設定されます
     */
    private native void open(String deviceName) throws IOException;

    /**
     * @事前条件 {@link #open(int)} を先に呼んでいる事
     */
    private native void read(int sectorNo, byte[] buffer) throws IOException;

    /**
     * @事前条件 {@link #open(int)} を先に呼んでいる事
     */
    private native void close() throws IOException;

    /** */
    static {
        System.loadLibrary("RawIO");
    }
}

/* */
