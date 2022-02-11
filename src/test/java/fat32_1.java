/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import vavi.util.StringUtil;

import vavix.io.WinRawIO;
import vavix.io.fat.Fat;
import vavix.io.fat.FileAllocationTable;
import vavix.io.fat.FileAllocationTable.DeletedDirectoryEntry;
import vavix.io.fat.FileEntry;
import vavix.io.fat.UserFat32;
import vavix.util.MatchingStrategy;


/**
 * fat32 forensic 1.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_1 {

    /**
     * @param args 0:indir, 1:outdir, 2:filelist
     */
    public static void main(String[] args) throws Exception {
        new fat32_1(args);
    }

    //-------------------------------------------------------------------------

    /** */
    private fat32_1(String[] args) throws Exception {
        String outdir = args[1];
//System.err.println(deviceName + ", " + path + ", " + file);
        this.fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        Map<String, FileEntry> entries = fat32.getEntries(args[0]);
//for (DirectoryEntry entry : entries.values()) {
// System.err.println(entry.getName() + ": " + entry.getStartCluster());
//}

//System.err.println("---- fill deleted clusters");
//        setUserCluster();
//System.err.println("----");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
        while (reader.ready()) {
            String file = reader.readLine();
            if (entries.containsKey(file)) {
                FileEntry entry = entries.get(file);
                if (entry instanceof DeletedDirectoryEntry) {
                    exec3((DeletedDirectoryEntry) entry, outdir, file);
                }
            } else {
System.err.println("file not found: " + file);
                reader.close();
                throw new FileNotFoundException();
            }
        }
        reader.close();
    }

    /** */
    FileAllocationTable fat32;

    /** */
    void setUserCluster() throws Exception {
        Fat fat = new UserFat32(fat32.bpb, fat32.fat);
        fat32.setFat(fat);
        //
        Scanner scanner = new Scanner(new FileInputStream("uc1.uc"));
        while (scanner.hasNextInt()) {
            // TSV
            int startCluster = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("startCluster: " + startCluster + ", size: " + size);
            List<Integer> clusters = new ArrayList<>(); 
            for (int i = 0; i < fat32.getRequiredClusters(size); i++) {
                clusters.add(startCluster + i);
            }
            fat.setClusterChain(clusters.toArray(new Integer[clusters.size()]));
        } 
        scanner.close();
        //
        scanner = new Scanner(new FileInputStream("uc2.uc"));
        while (scanner.hasNextInt()) {
            // TSV
            int size = scanner.nextInt();
            int startCluster = scanner.nextInt();
            int lastCluster = scanner.nextInt();
            int size2nd = scanner.nextInt();
System.err.println("startCluster: " + startCluster + ", size: " + size + ", lastCluster: "+ lastCluster + ", size2nd: " + size2nd);
            List<Integer> clusters = new ArrayList<>(); 
            int size1st = size - size2nd;
            int l = fat32.getRequiredClusters(size1st);
            for (int i = 0; i < l; i++) {
                clusters.add(startCluster + i);
            }
            l = fat32.getRequiredClusters(size2nd);
            for (int i = 0; i < l; i++) {
                clusters.add(lastCluster - l + 1 + i);
            }
            fat.setClusterChain(clusters.toArray(new Integer[clusters.size()]));
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /** */
    MatchingStrategy<byte[], ?> id3v2MatchingStrategy = new MatchingStrategy<byte[], Object>() {
        public int indexOf(byte[] pattern, Object dummy) {
            return pattern[0] == 'I' && pattern[1] == 'D' && pattern[2] == '3' ? 0 : -1;
        }
    };

    //-------------------------------------------------------------------------

    /**
     * 3: analyze intermittent file ok?
     */
    void exec3(DeletedDirectoryEntry entry, String outdir, String file) throws Exception {

        int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();
        byte[] buffer = new byte[bytesPerCluster]; 

        //
        // startClusterHigh を見つける
        //
        if (!entry.resolveStartCluster(id3v2MatchingStrategy) || !entry.isStartClusterValid()) {
System.err.println("start cluster not found: " + file);
            return;
        }
        int startCluster = entry.getStartCluster();

        //
        // 連続しているクラスターを書き出す
        // 途切れたら次の使われていないところ
        //

System.err.println(entry.getName() + ": " + entry.getStartCluster() + ", " + entry.length());

        int block = 0;
        boolean continued = false;
//outer:
        for (int cluster = 0; cluster < fat32.bpb.getLastCluster(); cluster++) {
            int targetCluster = startCluster + cluster;
System.err.print("cluster: " + targetCluster);

            // 途切れたら次の使われていないところ

            if (fat32.isUsing(targetCluster)) {
System.err.println(" has used, skip");
                continued = false;
                continue;
            } else {
                if (continued == false) {
                    continued = true;
                    block++;
System.err.println(" block: " + block);
                }
                fat32.readCluster(buffer, targetCluster);
if (block > 1) {
 System.err.println("\n" + StringUtil.getDump(buffer, 128));
}
            }

System.err.println();
        }

    }

    //-------------------------------------------------------------------------

    /**
     * 2: salvage intermittent file ok?
     */
    void exec2(DeletedDirectoryEntry entry, String outdir, String file) throws Exception {

        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()];

        //
        // startClusterHigh を見つける
        //
        if (!entry.resolveStartCluster(id3v2MatchingStrategy) || !entry.isStartClusterValid()) {
System.err.println("start cluster not found: " + file);
            return;
        }
        int startCluster = entry.getStartCluster();

        //
        // 連続しているクラスターを書き出す
        // 途切れたら次の使われていないところ
        //

        File output = new File(outdir, file);
        OutputStream os = new FileOutputStream(output);

System.err.println(entry.getName() + ": " + entry.getStartCluster() + ", " + entry.length());

        long rest = entry.length();


outer:
        for (int cluster = 0; cluster < fat32.bpb.getLastCluster(); cluster++) {
            int targetCluster = startCluster + cluster;
System.err.print("cluster: " + targetCluster);


            // 途切れたら次の使われていないところ

            if (fat32.isUsing(targetCluster)) {
System.err.println(" has used, skip");
//int restClusters = (int) ((rest + (fat32.getBytesPerCluster() - 1)) / fat32.getBytesPerCluster());
//System.err.println("rest: " + rest + " / " + entry.length() + ", " + restClusters + " clusters: " + file);
                continue;
            }

            // 1 クラスタの書き出し

            for (int sector = 0; sector < fat32.bpb.getSectorsPerCluster(); sector++) {
                int targetSector = fat32.bpb.toSector(targetCluster) + sector;
                fat32.io().readSector(buffer, targetSector);
                if (rest > fat32.bpb.getBytesPerSector()) {
                    os.write(buffer, 0, fat32.bpb.getBytesPerSector());
                    rest -= fat32.bpb.getBytesPerSector();
                } else {
                    os.write(buffer, 0, (int) rest);
                    rest -= rest;
                    break outer;
                }
            }
System.err.println(" salvaged: " + (entry.length() - rest) + "/" + entry.length());
        }

System.err.println(" salvaged, finish: " + (entry.length() - rest) + "/" + entry.length());


        os.flush();
        os.close();
        output.setLastModified(entry.lastModified());

    }

    //-------------------------------------------------------------------------

    /**
     * 1: salvage continued file only
     */
    void exec1(DeletedDirectoryEntry entry, String outdir, String file) throws Exception {

        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()];

        boolean incomplete = false;

        //
        // startClusterHigh を見つける
        //

        if (!entry.resolveStartCluster(id3v2MatchingStrategy) || !entry.isStartClusterValid()) {
System.err.println("start cluster not found: " + file);
            return;
        }
        int startCluster = entry.getStartCluster();

        //
        // 連続しているクラスターを書き出す
        // 途切れたら終わり
        //

        File output = new File(outdir, file);
        OutputStream os = new FileOutputStream(output);

System.err.println(entry.getName() + ": " + entry.getStartCluster() + ", " + entry.length());

        long rest = entry.length();


outer:
        for (int cluster = 0; cluster < fat32.bpb.getLastCluster(); cluster++) {
            int targetCluster = startCluster + cluster;
System.err.print("cluster: " + targetCluster);


            // 途切れたら終わり

            if (fat32.isUsing(targetCluster)) {
System.err.println(" has used, skip");
int restClusters = fat32.getRequiredClusters(rest);
System.err.println("rest: " + file + ": " + restClusters + " clusters, " + rest + " / " + entry.length());
                incomplete = true;
System.err.println("salvage, not continued: " + file + ": " + (entry.length() - rest) + " / " + entry.length());
                break outer;
            }


            // 1 クラスタの書き出し

            for (int sector = 0; sector < fat32.bpb.getSectorsPerCluster(); sector++) {
                int targetSector = fat32.bpb.toSector(targetCluster) + sector;
                fat32.io().readSector(buffer, targetSector);
                if (rest > fat32.bpb.getBytesPerSector()) {
                    os.write(buffer, 0, fat32.bpb.getBytesPerSector());
                    rest -= fat32.bpb.getBytesPerSector();
                } else {
                    os.write(buffer, 0, (int) rest);
                    rest -= rest;
                    break outer;
                }
            }
System.err.println(" salvaged: " + (entry.length() - rest) + " / " + entry.length());
        }

System.err.println("salvage finished: " + (entry.length() - rest) + "/" + entry.length());


        os.flush();
        os.close();
        output.setLastModified(entry.lastModified());

        // 途切れたら incomplete を付ける

        if (incomplete) {
            output.renameTo(new File(outdir, file + "." + "incomplete"));
        }
    }
}

/* */
