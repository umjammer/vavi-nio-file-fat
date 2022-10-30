package ipod;/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import vavix.io.WinRawIO;
import vavix.io.fat.DosFileEntry;
import vavix.io.fat.FileAllocationTable;
import vavix.io.fat.FileAllocationTable.DeletedFileEntry;
import vavix.io.fat.FileEntry;


/**
 * fat32 forensic 6.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/13 nsano initial version <br>
 */
public class fat32_6 {
    /**
     * search word in cluster
     * @param args 0:device
     */
    public static void main(String[] args) throws Exception {
        new fat32_6(args);
    }

    /** */
    FileAllocationTable fat32;

    /** */
    Comparator<DeletedFileEntry> createdAndNameComparator = (o1, o2) -> {
        if (o1.created() - o2.created() != 0) {
            return (int) (o1.created() / 1000 - o2.created() / 1000);
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    };

    /** */
    Comparator<DeletedFileEntry> createdComparator = (o1, o2) -> (int) (o1.created() / 1000 - o2.created() / 1000);

    /** */
    Comparator<DeletedFileEntry> lastModifiedComparator = (o1, o2) -> (int) (o1.lastModified() / 1000 - o2.lastModified() / 1000);

    /** */
    Comparator<DeletedFileEntry> lastAccessedComparator = (o1, o2) -> (int) (o1.lastAccessed() / (1000 * 60 * 60 * 24) - o2.lastAccessed() / (1000 * 60 * 60 * 24));

    /** */
//    FindingStrategy continuousClustersFindingStrategy = new FindingStrategy() {
//        public List<Integer> getClusterList(int startCluster) {
//            List<Integer> clusters;
//
//            for (int cluster = 0; cluster < fat32.getLastCluster(); cluster++) {
//                int targetCluster = startCluster + cluster;
//System.err.print("cluster: " + targetCluster);
//
//                // when it comes end, search next unused
//
//                if (isUsing(fat32, targetCluster)) {
//System.err.println(" has used, skip");
//int restClusters = (int) ((rest + (fat32.getBytesPerCluster() - 1)) / fat32.getBytesPerCluster());
//System.err.println("rest: " + rest + " / " + length() + ", " + restClusters + " clusters: " + file);
//                    continue;
//                } else {
//                    clusters.add(cluster);
//                }
//            }
//
//            return clusters;
//        }
//    };

    /** */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    fat32_6(String[] args) throws Exception {
        String deviceName = args[0];
        this.fat32 = new FileAllocationTable(new WinRawIO(deviceName));

        File cache = new File("deletedEntries.cache");
        List<FileAllocationTable.DeletedFileEntry> deletedEntries;
        if (!cache.exists()) {
            String path = deviceName;
            deletedEntries = new ArrayList<>();
            dig(path, deletedEntries);

            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cache.toPath()));
            oos.writeObject(deletedEntries);
            oos.flush();
            oos.close();
        } else {
            ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cache.toPath()));
            deletedEntries = (List) ois.readObject();
            ois.close();
        }

        deletedEntries.sort(createdAndNameComparator);
        for (DeletedFileEntry entry : deletedEntries) {
System.err.printf("%tF, %tF, %tF: %s, %d\n", entry.lastAccessed(), entry.lastModified(), entry.created(), entry.getName(), entry.getStartCluster());
        }

        // + file's lastCreated() is before the self lastAccessed() of deletion
        // - files deleted before the self lastAccessed() of deletion
    }

    /** */
    void dig(String path, List<FileAllocationTable.DeletedFileEntry> deletedEntries) throws IOException {
System.err.println("DIR: " + path);
        Map<String, FileEntry> entries = fat32.getEntries(path);
        for (FileEntry entry : entries.values()) {
            if (entry instanceof DosFileEntry && !(entry instanceof DeletedFileEntry)) {
                if (entry.isDirectory()) {
                    if (!entry.getName().equals(".") && !entry.getName().equals("..")) {
                        try {
                            dig(path + "\\" + entry.getName(), deletedEntries);
                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    }
                }
            } else if (entry instanceof FileAllocationTable.DeletedFileEntry) {
//System.err.printf("%s\\%s: %tF\n", path, entry.getName(), ((DeletedFileEntry) entry).lastAccessed());
                deletedEntries.add((FileAllocationTable.DeletedFileEntry) entry);
            }
        }
    }
}

/* */
