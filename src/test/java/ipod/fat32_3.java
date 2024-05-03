/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ipod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import vavi.util.StringUtil;
import vavix.io.WinRawIO;
import vavix.io.fat.DirectoryEntry;
import vavix.io.fat.FileAllocationTable;
import vavix.io.fat.FileEntry;
import vavix.util.ByteArrayMatcher;
import vavix.util.Matcher;


/**
 * fat32 forensic 3.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_3 {

    /** */
    public static void main(String[] args) throws Exception {
        exec2(args);
    }

    /**
     * 3: find specified location of id3v1 at the last cluster
     * @param args 0:deviceName, 1:last clusters file 2:size
     */
    static void exec3(String[] args) throws Exception {
        String deviceName = args[0];
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(deviceName));
        String file = args[1];
        int size = Integer.parseInt(args[2]);

System.err.println("file: " + file);
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(file)));
        boolean found = false;
        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()]; 
//outer:
        while (scanner.hasNextInt()) {
            int lastCluster = scanner.nextInt();

            int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();
            int rest = size % bytesPerCluster;

            for (int sector = 0; sector < fat32.bpb.getSectorsPerCluster(); sector++) {
                int targetSector = fat32.bpb.toSector(lastCluster) + sector;
                fat32.io().readSector(buffer, targetSector);
                Matcher<byte[]> matcher = new ByteArrayMatcher(buffer);
                int index = matcher.indexOf("TAG".getBytes(), 0); // id3 header
                if (index != -1 && index + 128 == rest) { // id3 size
System.err.println("found at cluster: " + lastCluster + "\n" + StringUtil.getDump(buffer));
                    found = true;
                    continue;
                } else {
//System.err.println("lastCluster: " + lastCluster + ", " + index + " , " + rest);
                }
                rest -= fat32.bpb.getBytesPerSector();
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

    // ----

    /**
     * 2: find firstClusterHigh by id3v2
     * @param args 0:indir, 1:filelist
     */
    static void exec2(String[] args) throws Exception {
        String deviceName = args[0];
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(deviceName));
        DirectoryEntry directory = fat32.getDirectoryEntry(args[0]);

        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()]; 
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(args[1]))));
        while (reader.ready()) {
            String file = reader.readLine();
System.err.println("file: " + file);
            FileEntry entry = directory.find(file);
            if (entry != null) {
                for (int i = 0; i < (fat32.bpb.getLastCluster() + 0xffff) / 0x10000; i++) {
                    int startCluster = i * 0x10000 + entry.getStartCluster();
System.err.print("cluster: " + startCluster);
                    int targetSector = fat32.bpb.toSector(startCluster);
                    fat32.io().readSector(buffer, targetSector);
                    if (buffer[0] == 'I' && buffer[1] == 'D' && buffer[2] == '3') {

                        // if used then next

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

    // ----

    /**
     * 1: check just existance.
     * @param args 0:indir, 1:filelist
     */
    public static void exec1(String[] args) throws Exception {
        String deviceName = args[0];
//System.err.println(deviceName + ", " + path + ", " + file);
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(deviceName));
        DirectoryEntry directory = fat32.getDirectoryEntry(args[0]);
//for (DirectoryEntry entry : entries.values()) {
// System.err.println(entry.getName() + ": " + entry.getStartCluster());
//}

        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(args[1]))));
        while (reader.ready()) {
            String file = reader.readLine();
            FileEntry entry = directory.find(file);
            if (entry != null) {
System.err.println(entry.getName() + "\n" + StringUtil.paramString(entry));
            } else {
System.err.println("not found: " + file);
            }
        }
        reader.close();
    }
}
