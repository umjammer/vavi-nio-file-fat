/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
     * DeletedFileEntry.
     * {@link DosFileEntry#lastAccessed()} is the date deleted.
     */
    public class DeletedFileEntry extends DosFileEntry {
        /** */
        private static final long serialVersionUID = -8752690030998809470L;
        /** */
        boolean startClusterValid = false;
        /** */
        protected String getPrefixString() {
            return "_";
        }
        /** */
        DeletedFileEntry(DataInput is) throws IOException {
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
        /** &gt;path, entries&lt; */
        Map<String, Map<String, FileEntry>> entriesMap = new HashMap<>();

        /**
         * @param path F:\xxx\yyy
         */
        Map<String, FileEntry> getEntries(String path) throws IOException {
            if (path.indexOf(':') == 1) {
                path = path.substring(2);
            }
            path = path.replaceFirst("\\\\$", "");
//Debug.println(Level.FINER, "**** path: [" + path + "]");
            Map<String, FileEntry> entries = entriesMap.get(path);
            if (entries != null) {
                return entries;
            } else {
                entries = entriesMap.get("");
                if (entries == null) {
                    entries = getEntries(bpb.getStartClusterOfRootDirectory());
//Debug.println(Level.FINER, "**** entries: \\ (" + bpb.getStartClusterOfRootDirectory() + "): " + entries.values());
                    entriesMap.put("", entries);
                }
            }
            if (path.equals("")) {
//Debug.println(Level.FINER, "<<<<<<<<<<<: \\: " + entries.values());
                return entries;
            }
            StringTokenizer st = new StringTokenizer(path, "\\");
            while (st.hasMoreTokens()) {
                String directory = st.nextToken();
//Debug.println(Level.FINER, "**** directory: [" + directory + "]");
                FileEntry entry = entries.get(directory);
                if (entry != null) {
                    entries = getEntries(entry.getStartCluster());
//Debug.println(Level.FINER, "**** entries: " + directory + " (" + entry.getStartCluster() + "): " + entries.values());
                    entriesMap.put(path + "\\" + directory, entries);
                } else {
                    throw new IllegalArgumentException("no such directory: " + directory);
                }
            }
//Debug.println(Level.FINER, "<<<<<<<<<<<: " + path + ": " + entries.values());
            return entries;
        }
        /** &lt;file name, file entry&gt; */
        Map<String, FileEntry> getEntries(int startCluster) throws IOException {
            SortedMap<String, FileEntry> entries = new TreeMap<>();
            Integer[] clusters = fat.getClusterChain(startCluster);
Debug.println(Level.FINE, "clusters: " + Arrays.toString(clusters) + ", spc: " + bpb.getSectorsPerCluster());
int fcs = (bpb.getFatSectors() / bpb.getSectorsPerCluster());
Debug.println(Level.FINE, "fat secs: " + bpb.getFatSectors() + ", sec per cluster: " + bpb.getSectorsPerCluster());
if (clusters.length > fcs) {
 Debug.println(Level.WARNING, "clusters is larger than definitions, shorten: " + clusters.length + " -> " + fcs);
 clusters = Arrays.copyOfRange(clusters, 0, fcs);
}
            List<LongNameFileEntry> deletedLongNames = new ArrayList<>();
            SortedSet<LongNameFileEntry> temporaryLongNames = new TreeSet<>();
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
                        case 0xe5: { // deleted
                            if (attributeByte == 0x0f) { // has long name
                                LongNameFileEntry fileEntry = new DeletedLongNameFileEntry(ledi);
                                deletedLongNames.add(0, fileEntry);
                            } else {
                                DeletedFileEntry fileEntry = new DeletedFileEntry(ledi);
//Debug.println(Level.FINE, StringUtil.paramString(fileEntry));
                                if (deletedLongNames.size() != 0) {
                                    fileEntry.setLongName(deletedLongNames);
                                    deletedLongNames.clear();
                                }
                                if (entries.containsKey(fileEntry.getName())) {
                                    entries.put(fileEntry.filename, fileEntry);
                                } else {
                                    entries.put(fileEntry.getName(), fileEntry);
                                }
                            }
                        }
                        break;
                        default: { // normal
                            if (attributeByte == 0x0f) { // has long name
                                LongNameFileEntry fileEntry = new LongNameFileEntry(ledi);
//Debug.println(Level.FINE, StringUtil.paramString(fileEntry));
                                temporaryLongNames.add(fileEntry);
                            } else {
                                FileEntry fileEntry = new DosFileEntry(ledi);
//Debug.println(Level.FINE, StringUtil.paramString(fileEntry));
                                if (temporaryLongNames.size() != 0) {
                                    fileEntry.setLongName(temporaryLongNames);
                                    temporaryLongNames.clear();
                                }
                                entries.put(fileEntry.getName(), fileEntry);
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

    /** @param buffer cluster bytes needed */
    public int readCluster(byte[] buffer, int cluster) throws IOException {
        byte[] buf = new byte[bpb.getBytesPerSector()];
        for (int sector = 0; sector < bpb.getSectorsPerCluster(); sector++) {
            int targetSector = bpb.toSector(cluster) + sector;
            io.readSector(buf, targetSector);
            System.arraycopy(buf, 0, buffer, bpb.getBytesPerSector() * sector, bpb.getBytesPerSector());
        }
        return bpb.getSectorsPerCluster() * bpb.getBytesPerSector();
    }

    private Directory directory = new Directory();

    /**
     * The entry point.
     * @return includes deleted entries
     */
    public Map<String, FileEntry> getEntries(String path) throws IOException {
        return directory.getEntries(path);
    }

    /** constructor */
    public FileAllocationTable(IOSource io) throws IOException {
        this.io = io;
        int bps = io.getBytesPerSector();
Debug.println(Level.FINE, "bps: " + bps);
        byte[] bytes = new byte[bps];
        io.readSector(bytes, 0);
        bpb = new ATBiosParameterBlock();
        Serdes.Util.deserialize(new ByteArrayInputStream(bytes), bpb);
        fat = bpb.getFatType();
Debug.println(Level.FINE, "fat: " + fat);
        ((FatType) fat).setRuntimeContext(io, bpb);
    }

    /** constructor */
    public FileAllocationTable(IOSource io, BiosParameterBlock bpb) throws IOException {
        this.io = io;
        this.bpb = bpb;
        fat = bpb.getFatType();
Debug.println(Level.FINE, "fat: " + fat);
        ((FatType) fat).setRuntimeContext(io, bpb);
    }

    /** get contents of the entry */
    public InputStream getData(FileEntry entry) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesPerCluster = bpb.getSectorsPerCluster() * bpb.getBytesPerSector();
        byte[] buf = new byte[bytesPerCluster];
        Integer[] clusterChain = fat.getClusterChain(entry.getStartCluster());
        for (int cluster : clusterChain) {
            int l = readCluster(buf, cluster);
//if (cluster == clusterChain[0]) {
// Debug.println(Level.FINE, "start cluster: " + cluster + " = sector: " + bpb.toSector(cluster) + ", " + + l + " bytes\n" + StringUtil.getDump(buf, 64));
//}
            if (cluster == clusterChain[clusterChain.length - 1]) {
                l = (int) (entry.length() % bytesPerCluster);
            }
            baos.write(buf, 0, l);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }
}

/* */
