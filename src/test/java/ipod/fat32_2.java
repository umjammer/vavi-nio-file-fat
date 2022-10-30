package ipod;/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import vavi.util.StringUtil;

import vavix.io.WinRawIO;
import vavix.io.fat.FileAllocationTable;
import vavix.util.ByteArrayMatcher;
import vavix.util.Matcher;


/**
 * fat32 forensic 2
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_2 {

    /** */
    public static void main(String[] args) throws Exception {
        exec1(args);
    }

    //-------------------------------------------------------------------------

    /**
     * 2: find clusters ID3v1 tag exsists
     * @param args 0:device
     */
    static void exec2(String[] args) throws Exception {
        String deviceName = args[0];
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(deviceName));

        int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();
        byte[] buffer = new byte[bytesPerCluster]; 
        int start = 3;
        for (int c = start; c < fat32.bpb.getLastCluster() + 0xffff; c++) {
            if (!fat32.isUsing(c)) {
                fat32.readCluster(buffer, c);
                Matcher<byte[]> matcher = new ByteArrayMatcher(buffer);
                int index = matcher.indexOf("TAG".getBytes(), 0);
                if (index != -1) {
                    System.err.println("cluster: " + c + " index: " + index + "\n" + StringUtil.getDump(buffer, index, 128));
                }
            }
        }
    }

    //-------------------------------------------------------------------------

    /**
     * 1: find clusters ID3v2 tag exsists
     * @param args 0:device
     */
    static void exec1(String[] args) throws Exception {
        String deviceName = args[0];
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(deviceName));

        int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();
        byte[] buffer = new byte[bytesPerCluster]; 
        int start = 3;
        for (int c = start; c < fat32.bpb.getLastCluster() + 0xffff; c++) {
            if (!fat32.isUsing(c)) {
                fat32.io().readSector(buffer, c);
                if (buffer[0] == 'I' && buffer[1] == 'D' && buffer[2] == '3') {
                    System.err.println("found cluster: " + c + "\n" + StringUtil.getDump(buffer, 128));
                }
            }
        }
    }
}

/* */
