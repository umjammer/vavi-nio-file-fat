/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;

import vavi.util.StringUtil;

import vavix.io.WinRawIO;
import vavix.io.fat32.Fat32;
import vavix.io.fat32.Fat32.FileEntry;


/**
 * fat32_3.
 * 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_3 {
    /**
     */
    public static void main(String[] args) throws Exception {
        exec2(args);
    }

    /**
     * 3: find specified location of id3v1 at the last cluster
     * @param args 0:deviceName, 1:last clusters file 2:size
     */
    static void exec3(String[] args) throws Exception {
        String deviceName = args[0];
        Fat32 fat32 = new Fat32(new WinRawIO(deviceName));
        String file = args[1];
        int size = Integer.parseInt(args[2]);

System.err.println("file: " + file);
        Scanner scanner = new Scanner(new FileInputStream(file));
        boolean found = false;
        byte[] buffer = new byte[fat32.getBytesPerSector()]; 
//outer:
        while (scanner.hasNextInt()) {
            int lastCluster = scanner.nextInt();

            int rest = size % fat32.getBytesPerCluster();

            for (int sector = 0; sector < fat32.getSectorsPerCluster(); sector++) {
                int targetSector = fat32.getSector(lastCluster) + sector;
                fat32.readSector(buffer, targetSector);
                int index = fat32.matcher(buffer).indexOf("TAG".getBytes(), 0); // id3 header
                if (index != -1 && index + 128 == rest) { // id3 size
System.err.println("found at cluster: " + lastCluster + "\n" + StringUtil.getDump(buffer));
                    found = true;
                    continue;
                } else {
//System.err.println("lastCluster: " + lastCluster + ", " + index + " , " + rest);
                }
                rest -= fat32.getBytesPerSector();
                if (rest < 0) {
                    break;
                }
            }
        }
        scanner.close();
        if (!found) {
            System.err.println("not found");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * 2: find firstClusterHigh by id3v2
     * @param args 0:indir, 1:filelist
     */
    static void exec2(String[] args) throws Exception {
        String deviceName = args[0];
        Fat32 fat32 = new Fat32(new WinRawIO(deviceName));
        Map<String, FileEntry> entries = fat32.getEntries(args[0]);

        byte[] buffer = new byte[fat32.getBytesPerSector()]; 
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
        while (reader.ready()) {
            String file = reader.readLine();
System.err.println("file: " + file);
            if (entries.containsKey(file)) {
                FileEntry entry = entries.get(file);

                for (int i = 0; i < (fat32.getLastCluster() + 0xffff) / 0x10000; i++) {
                    int startCluster = i * 0x10000 + entry.getStartCluster();
System.err.print("cluster: " + startCluster);
                    int targetSector = fat32.getSector(startCluster);
                    fat32.readSector(buffer, targetSector);
                    if (buffer[0] == 'I' && buffer[1] == 'D' && buffer[2] == '3') {
        
                        // 使われていたら次
        
                        if (!fat32.isUsing(startCluster)) {
System.err.println("startCluster: " + startCluster + ", startClusterHigh: " + i + "\n" + StringUtil.getDump(buffer));
                        }
                    } else {
System.err.println(", startClusterHigh: " + i + "\n" + StringUtil.getDump(buffer, 64));
                    }
                }
            }
        }
        reader.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 1: check just existance.
     * @param args 0:indir, 1:filelist
     */
    public static void exec1(String[] args) throws Exception {
        String deviceName = args[0];
//System.err.println(deviceName + ", " + path + ", " + file);
        Fat32 fat32 = new Fat32(new WinRawIO(deviceName));
        Map<String, FileEntry> entries = fat32.getEntries(args[0]);
//for (DirectoryEntry entry : entries.values()) {
// System.err.println(entry.getName() + ": " + entry.getStartCluster());
//}

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
        while (reader.ready()) {
            String file = reader.readLine();
            if (entries.containsKey(file)) {
                FileEntry entry = entries.get(file);
System.err.println(entry.getName() + "\n" + StringUtil.paramString(entry));
            } else {
System.err.println("not found: " + file);
            }
        }
        reader.close();
    }
}

/* */
