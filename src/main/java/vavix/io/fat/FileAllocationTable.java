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
import java.io.UncheckedIOException;
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
import java.util.stream.Collectors;

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
     * utility for forensic
     * Finds `startClusterHigh`.
     *
     * @return false if not found
     */
    public boolean resolveStartCluster(DeletedEntryImpl deletedFileEntry, MatchingStrategy<byte[], ?> matching) throws IOException {

        int startClusterHigh = -1;

        byte[] buffer = new byte[bpb.getBytesPerSector()];
        for (int i = 0; i < (bpb.getLastCluster() + 0xffff) / 0x10000; i++) {
            int startCluster = i * 0x10000 + deletedFileEntry.startCluster;
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
            deletedFileEntry.setStartClusterValid(true);
            System.err.println("startCluster: " + deletedFileEntry.startCluster + " -> " + (startClusterHigh * 0x10000 + deletedFileEntry.startCluster) + ", startClusterHigh: " + startClusterHigh + "\n" + StringUtil.getDump(buffer));
            deletedFileEntry.startCluster = startClusterHigh * 0x10000 + deletedFileEntry.startCluster;
            return true;
        } else {
            return false;
        }
    }

    /** utility for forensic */
    public int getRequiredClusters(long size) {
        int bytesPerCluster = bpb.getSectorsPerCluster() * bpb.getBytesPerSector();
        return (int) ((size + (bytesPerCluster - 1)) / bytesPerCluster);
    }

    /** */
    public void setFatNumber(int fatNumber) {
        if (fat instanceof FatType) {
            ((FatType) fat).setFatNumber(fatNumber);
        } else {
            throw new UnsupportedOperationException("current fat does not support the fat number: " + fatNumber);
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

    /**
     * utility, fill directory list for the specified directory.
     * @return deleted entries are not included
     */
    public DirectoryEntry fillEntries(DirectoryEntry directory) {
        try {
            if (directory.entries() == null) {
                List<FileEntry> entries = getDirectoryEntry(directory.getStartCluster()).stream()
                        .filter(e -> !(e instanceof DeletedEntryImpl))
                        .collect(Collectors.toList());
                directory.setEntries(entries);
            }
            return directory;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** &gt;path, directory&lt; */
    private static Map<String, DirectoryEntry> entriesMap = new HashMap<>();

    /**
     * The entry point.
     * @param path directory e.g. F:\xxx\yyy
     * @return directory entry represents the <code>path</code>, includes deleted entries
     */
    public DirectoryEntry getDirectoryEntry(String path) throws IOException {
        if (path.indexOf(':') == 1) {
            path = path.substring(2);
        }
        path = path.replaceFirst("\\\\$", "");
Debug.println(Level.FINEST, "**** path: [" + path + "]");
        DirectoryEntry directory = entriesMap.get(path);
        if (directory != null) {
            return directory;
        } else {
            directory = entriesMap.get("");
            if (directory == null) {
                directory = new DirectoryEntryImpl.RootDosDirectoryEntry(getDirectoryEntry(bpb.getStartClusterOfRootDirectory()));
Debug.println(Level.FINEST, "**** directory: \\ (" + bpb.getStartClusterOfRootDirectory() + "): " + directory.entries());
                entriesMap.put("", directory);
            }
        }
        if (path.equals("")) {
Debug.println(Level.FINEST, "<<<<<<<<<<<: \\: " + directory.entries());
            return directory;
        }
        StringTokenizer st = new StringTokenizer(path, "\\");
        while (st.hasMoreTokens()) {
            String dirName = st.nextToken();
Debug.println(Level.FINEST, "**** directory: [" + dirName + "]: " + directory);
            directory = (DirectoryEntry) directory.find(dirName);
            if (directory != null) {
                fillEntries(directory);
Debug.println(Level.FINEST, "**** directory: " + directory + " (" + directory.getStartCluster() + "): " + directory.entries());
                entriesMap.put(path + "\\" + dirName, directory);
            } else {
                throw new IllegalArgumentException("no such directory: " + dirName);
            }
        }
Debug.println(Level.FINEST, "<<<<<<<<<<<: " + path + ": " + directory.entries());
        return directory;
    }

    /** gets directory list for the specified start cluster of the directory. */
    private List<FileEntry> getDirectoryEntry(int startCluster) throws IOException {
        SortedMap<String, FileEntry> entries = new TreeMap<>();
        Integer[] clusters = fat.getClusterChain(startCluster);
Debug.println(Level.FINE, "dir clusters: start: " + startCluster + ", " + Arrays.toString(clusters) + ", sector/cluster: " + bpb.getSectorsPerCluster());
if (clusters.length > 2) {
 Debug.println(Level.WARNING, "clusters is larger than 2" + clusters.length);
}
        List<LongNameFileEntry> deletedLongNames = new ArrayList<>();
        SortedSet<LongNameFileEntry> temporaryLongNames = new TreeSet<>();
        for (int cluster : clusters) {
            for (int sector = 0; sector < bpb.getSectorsPerCluster(); sector++) {
//Debug.println(Level.FINEST, "sector: " + (bpb.toSector(cluster) + sector));
                byte[] buffer = new byte[1024];
                io.readSector(buffer, bpb.toSector(cluster) + sector);
                for (int entry = 0; entry < io.getBytesPerSector() / 32; entry++) {
//Debug.println(Level.FINEST, "entry:\n" + StringUtil.getDump(buffer, entry * 32, 32));
                    DataInput ledi = new LittleEndianDataInputStream(new ByteArrayInputStream(buffer, entry * 32, 32));
                    int firstByte = buffer[entry * 32] & 0xff;
                    int attributeByte = buffer[entry * 32 + 0x0b];
                    switch (firstByte) {
                    case 0x00:
//Debug.println(Level.FINEST, "none");
                        break;
                    case 0xe5: { // deleted
                        if (attributeByte == 0x0f) { // long name entry
                            // we assume LFNs for a SFN are located just before SFN
                            LongNameFileEntry fileEntry = new DeletedLongNameFileEntry(ledi);
                            deletedLongNames.add(0, fileEntry);
                        } else {
                            DeletedEntry fileEntry = new DeletedEntryImpl(ledi);
//Debug.println(Level.FINEST, StringUtil.paramString(fileEntry));
                            if (deletedLongNames.size() != 0) {
                                fileEntry.setLongName(deletedLongNames);
                                deletedLongNames.clear();
                            }
                            if (entries.containsKey(fileEntry.getName())) {
                                entries.put(fileEntry.getName(), fileEntry);
                            } else {
                                entries.put(fileEntry.getName(), fileEntry);
                            }
                        }
                    }
                    break;
                    default: { // normal
                        if (attributeByte == 0x0f) { // long name entry
                            // we assume LFNs for a SFN are located just before SFN
                            LongNameFileEntry fileEntry = new LongNameFileEntry(ledi);
//Debug.println(Level.FINEST, StringUtil.paramString(fileEntry));
                            temporaryLongNames.add(fileEntry);
                        } else {
                            FileEntry fileEntry = new FileEntryImpl(ledi);
//Debug.println(Level.FINEST, StringUtil.paramString(fileEntry));
                            if (temporaryLongNames.size() != 0) {
                                fileEntry.setLongName(temporaryLongNames);
                                temporaryLongNames.clear();
                            }
                            if (!fileEntry.isDirectory()) {
                                entries.put(fileEntry.getName(), fileEntry);
                            } else {
                                entries.put(fileEntry.getName(), new DirectoryEntryImpl(fileEntry));
                            }
                        }
                    }
                    break;
                    }
                }
            }
        }
//for (String name : entries.keySet()) {
// Debug.printf(Level.FINEST, "%s: %d, %08x\n", name, entries.get(name).getStartCluster(), entries.get(name).getStartCluster());
//}
        return new ArrayList<>(entries.values());
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
Debug.println(Level.FINE, "bpb: " + bpb);
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
    public InputStream getInputStream(FileEntry entry) throws IOException {
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
