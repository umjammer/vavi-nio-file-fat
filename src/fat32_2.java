/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import vavi.util.StringUtil;
import vavix.io.fat32.Fat32;


/**
 * fat32_2. 
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
        Fat32 fat32 = new Fat32(deviceName);

        byte[] buffer = new byte[fat32.getBytesPerCluster()]; 
        int start = 3;
        for (int c = start; c < fat32.getLastCluster() + 0xffff; c++) {
            if (!fat32.isUsing(c)) {
                fat32.readCluster(buffer, c);
                int index = fat32.matcher(buffer).indexOf("TAG".getBytes(), 0);
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
        Fat32 fat32 = new Fat32(deviceName);

        byte[] buffer = new byte[fat32.getBytesPerCluster()]; 
        int start = 3;
        for (int c = start; c < fat32.getLastCluster() + 0xffff; c++) {
            if (!fat32.isUsing(c)) {
                fat32.readSector(buffer, c);
                if (buffer[0] == 'I' && buffer[1] == 'D' && buffer[2] == '3') {
                    System.err.println("found cluster: " + c + "\n" + StringUtil.getDump(buffer, 128));
                }
            }
        }
    }
}

/* */
