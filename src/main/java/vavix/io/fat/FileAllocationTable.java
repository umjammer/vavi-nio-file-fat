/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.serdes.Serdes;

import vavix.io.IOSource;
import vavix.util.Matcher;
import vavix.util.MatchingStrategy;
import vavix.util.StrategyPatternMatcher;


/**
 * FileAllocationTable.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060108 nsano initial version <br>
 */
public class FileAllocationTable implements Serializable {

    /** */
    private static final long serialVersionUID = -3299419883884944569L;

    /** */
    protected transient IOSource io;

    /** */
    public BiosParameterBlock bpb;

    /** */
    public Fat fat;

    /** */
    public IOSource io() {
        return io;
    }

    /**
     * DeletedDirectoryEntry.
     * {@link DosDirectoryEntry#lastAccessed()} is the date deleted.
     */
    public class DeletedDirectoryEntry extends DosDirectoryEntry {
        /** */
        private static final long serialVersionUID = -8752690030998809470L;
        /** */
        boolean startClusterValid = false;
        /** */
        protected String getPrefixString() {
            return "_";
        }
        /** */
        DeletedDirectoryEntry(DataInput is) throws IOException {
            super(is);
        }
        /**
         * Finds `startClusterHigh`.
         * @return false if not found
         */
        public boolean resolveStartCluster(MatchingStrategy<byte[], ?> matching) throws IOException {

            int startClusterHigh = -1;

            byte[] buffer = new byte[bpb.getBytesPerSector()];
            for (int i = 0; i < (bpb.getLastCluster() + 0xffff) / 0x10000; i++) {
                int startCluster = i * 0x10000 + this.startCluster;
                int targetSector = bpb.toSector(startCluster);
                io.readSector(buffer, targetSector);
                Matcher<MatchingStrategy<byte[], ?>> matcher = new StrategyPatternMatcher<>(buffer);
                if (matcher.indexOf(matching, 0) != -1) {

                    // next if it used.

                    if (!isUsing(startCluster)) {
                        startClusterHigh = i;
                        break;
                    }
                }
System.err.println("skip: " + i);
            }

            if (startClusterHigh != -1) {
                startClusterValid = true;
System.err.println("startCluster: " + this.startCluster + " -> " + (startClusterHigh * 0x10000 + this.startCluster) + ", startClusterHigh: " + startClusterHigh + "\n" + StringUtil.getDump(buffer));
                this.startCluster = startClusterHigh * 0x10000 + this.startCluster;
                return true;
            } else {
                return false;
            }
        }
        /** */
        public boolean isStartClusterValid() {
            return startClusterValid;
        }
    }

    /** */
    class Directory {
        /** */
        Map<String, FileEntry> entries;
        /**
         * @param path F:\xxx\yyy
         */
        Directory(String path) throws IOException {
//Debug.println(Level.FINE, "**** directory: \\");
            if (path.indexOf(':') == 1) {
                path = path.substring(2);
            }
            entries = getEntries(bpb.getStartClusterOfRootDirectory());
            StringTokenizer st = new StringTokenizer(path, "\\");
            while (st.hasMoreTokens()) {
                String directory = st.nextToken();
//Debug.println(Level.FINE, "**** directory: " + directory);
                if (entries.containsKey(directory)) {
                    entries = getEntries(entries.get(directory).getStartCluster());
                } else {
                    throw new IllegalArgumentException("no such directory: " + directory);
                }
            }
        }
        /** */
        Map<String, FileEntry> getEntries(int startCluster) throws IOException {
            SortedMap<String, FileEntry> entries = new TreeMap<>();
            Integer[] clusters = fat.getClusterChain(startCluster);
Debug.println(Level.FINE, "clusters: " + Arrays.toString(clusters));
int fcs = (bpb.getFatSectors() / bpb.getSectorsPerCluster());
Debug.println(Level.FINE, "fat secs: " + bpb.getFatSectors() + ", sec per cluster: " + bpb.getSectorsPerCluster());
if (clusters.length > fcs) {
 clusters = Arrays.copyOfRange(clusters, 0, fcs);
 Debug.println(Level.WARNING, "clusters is larger than definitions, shorten: " + clusters.length);
}
            List<LongNameDirectoryEntry> deletedLongNames = new ArrayList<>();
            SortedSet<LongNameDirectoryEntry> tempraryLongNames = new TreeSet<>();
            for (int cluster : clusters) {
                for (int sector = 0; sector < bpb.getSectorsPerCluster(); sector++) {
//Debug.println(Level.FINE, "sector: " + (bpb.getSector(clusters[cluster]) + sector));
                    byte[] buffer = new byte[1024];
                    io.readSector(buffer, bpb.toSector(cluster) + sector);
                    for (int entry = 0; entry < io.getBytesPerSector() / 32; entry++) {
//Debug.println(Level.FINE, "entry:\n" + StringUtil.getDump(buffer, entry * 32, 32));
                        DataInput ledi = new LittleEndianDataInputStream(new ByteArrayInputStream(buffer, entry * 32, 32));
                        int firstByte = buffer[entry * 32] & 0xff;
                        int attributeByte = buffer[entry * 32 + 0x0b];
                        switch (firstByte) {
                        case 0x00:
//Debug.println(Level.FINE, "none");
                            break;
                        case 0xe5: {
                            if (attributeByte == 0x0f) {
                                LongNameDirectoryEntry directoryEntry = new DeletedLongNameDirectoryEntry(ledi);
                                deletedLongNames.add(0, directoryEntry);
                            } else {
                                FileEntry directoryEntry = new DeletedDirectoryEntry(ledi);
//Debug.println(Level.FINE, StringUtil.paramString(directoryEntry));
                                if (deletedLongNames.size() != 0) {
                                    directoryEntry.setLongName(deletedLongNames);
                                    deletedLongNames.clear();
                                }
                                if (entries.containsKey(directoryEntry.getName())) {
                                    entries.put(((DeletedDirectoryEntry) directoryEntry).filename, directoryEntry);
                                } else {
                                    entries.put(directoryEntry.getName(), directoryEntry);
                                }
                            }
                        }
                        break;
                        default: {
                            if (attributeByte == 0x0f) {
                                LongNameDirectoryEntry directoryEntry = new LongNameDirectoryEntry(ledi);
//Debug.println(Level.FINE, StringUtil.paramString(directoryEntry));
                                tempraryLongNames.add(directoryEntry);
                            } else {
                                FileEntry directoryEntry = new DosDirectoryEntry(ledi);
//Debug.println(Level.FINE, StringUtil.paramString(directoryEntry));
                                if (tempraryLongNames.size() != 0) {
                                    directoryEntry.setLongName(tempraryLongNames);
                                    tempraryLongNames.clear();
                                }
                                entries.put(directoryEntry.getName(), directoryEntry);
                            }
                        }
                        break;
                        }
                    }
                }
            }
//for (String name : entries.keySet()) {
// Debug.printf(Level.FINE, "%s: %d, %08x\n", name, entries.get(name).getStartCluster(), entries.get(name).getStartCluster());
//}
            return entries;
        }
    }

    /** utility */
    public int getRequiredClusters(long size) {
        int bytesPerCluster = bpb.getSectorsPerCluster() * bpb.getBytesPerSector();
        return (int) ((size + (bytesPerCluster - 1)) / bytesPerCluster);
    }

    /** */
    public void setFatNumber(int fatNumber) {
        if (fat instanceof FatType) {
            ((FatType) fat).setFatNumber(fatNumber);
        } else {
            throw new UnsupportedOperationException("current fat is not support fat number");
        }
    }

    /** */
    public void setFat(Fat fat) throws IOException {
        this.fat = fat;
    }

    /** TODO naming */
    public final boolean isUsing(int cluster) throws IOException {
        return fat.isUsing(cluster);
    }

    /** */
    public int readCluster(byte[] buffer, int cluster) throws IOException {
        byte[] buf = new byte[bpb.getBytesPerSector()];
        for (int sector = 0; sector < bpb.getSectorsPerCluster(); sector++) {
            int targetSector = bpb.toSector(cluster) + sector;
            io.readSector(buf, targetSector);
            System.arraycopy(buf, 0, buffer, bpb.getBytesPerSector() * sector, bpb.getBytesPerSector());
        }
        return bpb.getSectorsPerCluster() * bpb.getBytesPerSector();
    }

    /**
     * The entry point.
     */
    public Map<String, FileEntry> getEntries(String path) throws IOException {
        Directory directory = new Directory(path);
        return directory.entries;
    }

    /** */
    public FileAllocationTable(IOSource io) throws IOException {
        this.io = io;
        int bps = io.getBytesPerSector();
Debug.println("bps: " + bps);
        byte[] bytes = new byte[bps];
        io.readSector(bytes, 0);
        bpb = new ATBiosParameterBlock();
        Serdes.Util.deserialize(new ByteArrayInputStream(bytes), bpb);
        fat = bpb.getFatType();
Debug.println("fat: " + fat);
        ((FatType) fat).setRuntimeContext(io, bpb);
    }

    /** */
    public FileAllocationTable(IOSource io, BiosParameterBlock bpb) throws IOException {
        this.io = io;
        this.bpb = bpb;
        fat = bpb.getFatType();
Debug.println("fat: " + fat);
        ((FatType) fat).setRuntimeContext(io, bpb);
    }
}

/* */
