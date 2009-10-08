/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;

import vavi.util.StringUtil;


/**
 * RawIO. 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060108 nsano initial version <br>
 */
public class RawIO {

    /** */
    public RawIO(String deviceName) throws IOException {
        open(deviceName);
    }

    /** TODO deprecate? */
    public byte[] readSector(int sectorNo) throws IOException {
        byte[] buffer = new byte[bytesPerSector];
        read(sectorNo, buffer);
        return buffer;
    }

    /** */
    public int readSector(byte[] buffer, int sectorNo) throws IOException {
        read(sectorNo, buffer);
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

    //----

    /** */
    public static void main(String[] args) throws Exception {
        exec2(args);
    }

    /** */
    public static void exec2(String[] args) throws Exception {
        RawIO win32IO = new RawIO("\\\\.\\" + args[0]);
        byte[] buffer = win32IO.readSector(0);
        System.err.println(StringUtil.getDump(buffer));
    }

    /** */
    public static void exec1(String[] args) throws Exception {
        for (int i = 0; i < 16; i++) {
            try {
                RawIO win32IO = new RawIO("\\\\.\\PhysicalDrive" + i);
                byte[] buffer = win32IO.readSector(0);
                System.err.println(StringUtil.getDump(buffer));
            } catch (IOException e) {
                System.err.println(e);
                System.err.println("no drive: " + i);
            }
        }
    }
}

/* */
