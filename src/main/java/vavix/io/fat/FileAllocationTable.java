/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.awt.dnd.DropTargetListener;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.ByteUtil;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.injection.Element;
import vavi.util.injection.Injector;
import vavi.util.win32.DateUtil;

import vavix.io.IOSource;
import vavix.util.Matcher;
import vavix.util.MatchingStrategy;
import vavix.util.StrategyPatternMatcher;


/**
 * Fat32.
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

    public interface BiosParameterBlock {
        int getSectorsPerCluster();
        int getStartClusterOfRootDirectory();
        int getBytesPerSector();
        int getFatSector(int fatNumber);
        /** for deleted, or write */
        int getLastCluster();
        int toSector(int cluster);
        int getFatSectors();
        FatType getFatType();
    }

    @Injector(bigEndian = false)
    public static class BPB implements BiosParameterBlock, Serializable {
        /** */
        private static final long serialVersionUID = -1066456664696979810L;
        @Element(sequence = 1)
        byte[] jump = new byte[3];
        /** OEM name */
        @Element(sequence = 2, value = "8")
        String oemLavel;
        /** logical sector size */
        @Element(sequence = 3, value = "unsigned short")
        int bytesPerSector;
        /** number of sectors per a cluster (allocation unit) */
        @Element(sequence = 4, value = "unsigned byte")
        int sectorsPerCluster;
        /** */
        @Element(sequence = 5, value = "unsigned short")
        int reservedSectors;
        /** number of FA */
        @Element(sequence = 6, value = "unsigned byte")
        int numberOfFAT;
        /**
         * max size of root directory entries.
         * for FAT12/16，always 0000h for FAT32.
         */
        @Element(sequence = 7, value = "unsigned short")
        int maxRootDirectoryEntries;
        /**
         * number of all sectors (Small Sector)
         * always 0000h for FAT32.
         * the case of the number of all sectors can be represented by WORD(2bytes).
         */
        @Element(sequence = 8, value = "unsigned short")
        int numberOfSmallSectors;
        /** */
        @Element(sequence = 9, value = "unsigned byte")
        int mediaDescriptor;
        /** */
        @Element(sequence = 10, value = "unsigned short")
        int numberOfFATSector;
        /** */
        @Element(sequence = 11, value = "unsigned short")
        int sectorsPerTrack;
        /** */
        @Element(sequence = 12, value = "unsigned short")
        int headsPerDrive;
        /** */
        @Element(sequence = 13)
        int invisibleSectors;
        /** */
        @Element(sequence = 14)
        public long numberOfLargeSectors;
        /** */
        @Element(sequence = 15, value = "3")
        int sectorsPerFAT;
        /** */
        @Element(sequence = 16, value = "unsigned short")
        int mediaDescriptionFlag;
        /** */
        @Element(sequence = 17, value = "unsigned short")
        int fileSystemVersion;
        /** the ROOT directory */
        @Element(sequence = 18)
        int startClusterOfRootDirectory;
        /** */
        @Element(sequence = 19, value = "unsigned short")
        int sectorOfFSInfo;
        /** */
        @Element(sequence = 20, value = "unsigned short")
        int sectorOfCopyBootSector;
        @Element(sequence = 21)
        byte[] b3 = new byte[12];
        /** */
        @Element(sequence = 22, value = "unsigned byte")
        int physicalDriveNumber;
        @Element(sequence = 23)
        byte b4;
        /** */
        @Element(sequence = 24, value = "unsigned byte")
        int bootSignature;
        /** */
        @Element(sequence = 25)
        int volumeSerialID;
        /** */
        @Element(sequence = 26, value = "11")
        String volumeLavel;
        /** */
        @Element(sequence = 27, value = "8")
        String fileSystemType;
        @Override
        public String toString() {
            return String
                    .format("BPB [jump=%s, oemLavel=%s, bytesPerSector=%s, sectorsPerCluster=%s, reservedSectors=%s, numberOfFAT=%s, maxRootDirectoryEntries=%s, numberOfSmallSectors=%s, mediaDescriptor=%s, numberOfFATSector=%s, sectorsPerTrack=%s, headsPerDrive=%s, invisibleSectors=%s, numberOfLargeSectors=%s, sectorsPerFAT=%s, mediaDescriptionFlag=%s, fileSystemVersion=%s, startClusterOfRootDirectory=%s, sectorOfFSInfo=%s, sectorOfCopyBootSector=%s, b3=%s, physicalDriveNumber=%s, b4=%s, bootSignature=%s, volumeSerialID=%s, volumeLavel=%s, fileSystemType=%s]",
                            Arrays.toString(jump), oemLavel, bytesPerSector,
                            sectorsPerCluster, reservedSectors, numberOfFAT,
                            maxRootDirectoryEntries, numberOfSmallSectors, mediaDescriptor,
                            numberOfFATSector, sectorsPerTrack, headsPerDrive,
                            invisibleSectors, numberOfLargeSectors, sectorsPerFAT,
                            mediaDescriptionFlag, fileSystemVersion, startClusterOfRootDirectory, sectorOfFSInfo,
                            sectorOfCopyBootSector, Arrays.toString(b3), physicalDriveNumber, b4,
                            bootSignature, volumeSerialID, volumeLavel, fileSystemType);
        }
        @Override
        public int getSectorsPerCluster() {
            return sectorsPerCluster;
        }
        @Override
        public int getLastCluster() {
            return (int) ((numberOfLargeSectors + (sectorsPerCluster - 1)) / sectorsPerCluster);
        }
        @Override
        public int getFatSector(int fatNumber) {
            return reservedSectors + fatNumber * sectorsPerFAT;
        }
        @Override
        public int getStartClusterOfRootDirectory() {
            return startClusterOfRootDirectory;
        }
        @Override
        public int getBytesPerSector() {
            return bytesPerSector;
        }
        @Override
        public int toSector(int cluster) {
            int sector = (cluster - 2) * sectorsPerCluster + reservedSectors + sectorsPerFAT * numberOfFAT;
            return sector;
        }
        @Override
        public int getFatSectors() {
            return sectorsPerFAT;
        }
        @Override
        public FatType getFatType() {
            switch (fileSystemType.replaceFirst("\\s*$", "")) {
            default:
            case "FAT12":
                return FatType.Fat12Fat;
            case "FAT16":
                return FatType.Fat16Fat;
            case "FAT32":
                return FatType.Fat32Fat;
            }
        }
    }

    /** */
    public interface Fat extends Serializable {
        /**
         * @param cluster startCluster
         */
        Integer[] getClusterChain(int cluster) throws IOException;
        /** */
        boolean isUsing(int cluster) throws IOException;
        /** */
        void setClusterValue(int cluster, int value) throws IOException;
        /** */
        int getClusterValue(int cluster) throws IOException;
        /** */
        void setClusterChain(Integer[] clusters) throws IOException;
    }

    /** */
    public static class UserFat32 implements Fat {
        /** */
        private static final long serialVersionUID = -6771019237363727447L;
        /** */
        int[] clusters;
        /** */
        public UserFat32(BiosParameterBlock bpb, Fat fat) throws IOException {
            int size = bpb.getLastCluster();
            clusters = new int[size];
            for (int i = 0; i < size; i++) {
                clusters[i] = fat.getClusterValue(i);
            }
        }
        /** */
        public Integer[] getClusterChain(int cluster) throws IOException {
            List<Integer> clusters = new ArrayList<>();
            do {
                clusters.add(cluster);
                cluster = this.clusters[cluster];
            } while (0000_0002 <= cluster && cluster <= 0x0fff_fff6);
            return clusters.toArray(new Integer[clusters.size()]);
        }
        /** */
        public boolean isUsing(int cluster) throws IOException {
            int value = getClusterValue(cluster);
            return 0000_0002 <= value && value <= 0x0fff_ffff;
        }
        /** */
        public void setClusterValue(int cluster, int value) throws IOException {
            clusters[cluster] = value;
        }
        /** */
        public int getClusterValue(int cluster) throws IOException {
            return clusters[cluster];
        }
        /** */
        public void setClusterChain(Integer[] clusters) throws IOException {
            for (int i = 0; i < clusters.length; i++) {
                if (i == clusters.length - 1) {
                    clusters[i] = 0x0fff_ffff;
                } else {
                    clusters[i] = clusters[i + 1];
                }
            }
        }
    }

    /** */
    enum FatType implements Fat {
        /**
         * <li>0000_0000h               unused cluster
         * <li>0000_0002h ~ 0fff_fff6h  next cluster for a file
         * <li>0fff_fff7h               bad cluster
         * <li>0fff_fff8h ~ 0fff_ffffh  last cluster for a file (used 0fff_ffffh normally)
         * @param cluster cluster
         */
        Fat32Fat {
            /** */
            private static final long serialVersionUID = -8008307331300948383L;
            @Override
            protected int nextCluster(int cluster) throws IOException {
                int sector = getFatSector() + (cluster * 4) / bpb.getBytesPerSector();
                if (sector != currentSector) {
                    io.readSector(buffer, sector);
                    currentSector = sector;
                }
                int position = (cluster * 4) % bpb.getBytesPerSector();
//Debug.println(Level.FINE, "sector: " + sector + "\n" + StringUtil.getDump(buffer, position, 8));
                int nextCluster = ByteUtil.readLeInt(buffer, position);
//Debug.printf(Level.FINE, "cluster: %1$d, sector: %2$d, position: %3$d, %3$08x, next: %d%n", cluster, sector, position, nextCluster);
                return nextCluster;
            }
            @Override
            public Integer[] getClusterChain(int cluster) throws IOException {
                List<Integer> clusters = new ArrayList<>();
                do {
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                    clusters.add(cluster);
                    cluster = nextCluster(cluster);
                } while (0000_0002 <= cluster && cluster <= 0x0fff_fff6);
                return clusters.toArray(new Integer[clusters.size()]);
            }
            @Override
            public boolean isUsing(int cluster) throws IOException {
                cluster = nextCluster(cluster);
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                return 0000_0002 <= cluster && cluster <= 0x0fff_ffff;
            }
        },
        Fat16Fat {
            /** */
            private static final long serialVersionUID = 1846975683407080677L;
            @Override
            protected int nextCluster(int cluster) throws IOException {
                int sector = getFatSector() + (cluster * 2) / bpb.getBytesPerSector();
                if (sector != currentSector) {
                    io.readSector(buffer, sector);
                    currentSector = sector;
                }
                int position = (cluster * 2) % bpb.getBytesPerSector();
Debug.println(Level.FINE, "sector: " + sector + "\n" + StringUtil.getDump(buffer, position, 8));
                int nextCluster = ByteUtil.readLeShort(buffer, position) & 0xffff;
Debug.printf(Level.FINE, "cluster: %1$d, sector: %2$d, position: %3$d, %3$08x, next: %d%n", cluster, sector, position, nextCluster);
                return nextCluster;
            }
            @Override
            public Integer[] getClusterChain(int cluster) throws IOException {
                List<Integer> clusters = new ArrayList<>();
                do {
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                    clusters.add(cluster);
                    cluster = nextCluster(cluster);
                } while (0002 <= cluster && cluster <= 0xfff6);
                return clusters.toArray(new Integer[clusters.size()]);
            }
            @Override
            public boolean isUsing(int cluster) throws IOException {
                cluster = nextCluster(cluster);
Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                return 0002 <= cluster && cluster <= 0xffff;
            }
        },
        Fat12Fat {
            /** */
            private static final long serialVersionUID = -3706950356198032944L;
            @Override
            protected int nextCluster(int cluster) throws IOException {
                int sector = getFatSector() + (cluster + cluster / 2) / bpb.getBytesPerSector();
                if (sector != currentSector) {
                    io.readSector(buffer, sector);
                    currentSector = sector;
                }
                int position = (cluster + cluster / 2) % bpb.getBytesPerSector();
//Debug.println(Level.FINE, "sector: " + sector + "\n" + StringUtil.getDump(buffer, position, 8));
                int nextCluster = (ByteUtil.readLeShort(buffer, position) >> ((cluster & 1) != 0 ? 4 : 0)) & 0x0fff;
//Debug.printf(Level.FINE, "cluster: %1$d, sector: %2$d, position: %3$d, %3$08x, next: %4$d%n", cluster, sector, position, nextCluster);
                return nextCluster;
            }
            @Override
            public Integer[] getClusterChain(int cluster) throws IOException {
                List<Integer> clusters = new ArrayList<>();
                do {
//Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                    clusters.add(cluster);
                    cluster = nextCluster(cluster);
                } while (002 <= cluster && cluster <= 0xff6);
                return clusters.toArray(new Integer[clusters.size()]);
            }
            @Override
            public boolean isUsing(int cluster) throws IOException {
                cluster = nextCluster(cluster);
//Debug.printf(Level.FINE, "cluster: %08x\n", cluster);
                return 002 <= cluster && cluster <= 0xfff;
            }
        };
        protected IOSource io;
        protected BiosParameterBlock bpb;
        public void setRuntimeContext(IOSource io, BiosParameterBlock bpb) {
            this.io = io;
            this.bpb = bpb;
        }
        /** */
        private int fatNumber = 0;
        /** */
        public void setFatNumber(int fatNumber) {
            this.fatNumber = fatNumber;
        }
        /** */
        protected int getFatSector() {
             return bpb.getFatSector(fatNumber);
        }
        /** TODO thread unsafe */
        protected byte[] buffer = new byte[1024];
        /** TODO thread unsafe */
        protected int currentSector = -1;
        /** TODO thread unsafe */
        protected abstract int nextCluster(int cluster) throws IOException;
        /** */
        public void setClusterValue(int cluster, int value) throws IOException {
            throw new UnsupportedOperationException("read only, use #useUserFat()");
        }
        /** TODO thread unsafe */
        public int getClusterValue(int cluster) throws IOException {
            return nextCluster(cluster);
        }
        /** */
        public void setClusterChain(Integer[] clusters) throws IOException {
            throw new UnsupportedOperationException("read only, use #useUserFat()");
        }
    }

    /** */
    public interface DirectoryEntry extends Serializable {
    }

    /** */
    static class DeletedLongNameDirectoryEntry extends LongNameDirectoryEntry {
        /** */
        private static final long serialVersionUID = -3509895194089903860L;

        DeletedLongNameDirectoryEntry(DataInput is) throws IOException {
            super(is);
            subEntryNo = -1;
            isLast = false;
        }
    }

    /** */
    static class LongNameDirectoryEntry implements DirectoryEntry, Comparable<LongNameDirectoryEntry> {
        /** */
        private static final long serialVersionUID = 1640728749170150017L;
        /** */
        int subEntryNo;
        /** */
        boolean isLast;
        /** */
        int attribute;
        /** */
        String filename;
        /** */
        int shortNameCheckSum;
        /** */
        LongNameDirectoryEntry(DataInput leis) throws IOException {
            int sequenceByte = leis.readUnsignedByte();
            subEntryNo = sequenceByte & 0x3f;
            isLast = (sequenceByte & 0x40) != 0;
            byte[] b1 = new byte[10];
            leis.readFully(b1);
            filename = new String(b1, 0, 10, Charset.forName("UTF-16LE"));
            attribute = leis.readUnsignedByte(); 
            leis.readUnsignedByte(); // longEntryType
            shortNameCheckSum = leis.readUnsignedByte();
            byte[] b2 = new byte[12];
            leis.readFully(b2);
            filename += new String(b2, 0, 12, Charset.forName("UTF-16LE"));
            byte[] b3 = new byte[2];
            leis.readFully(b3);
            byte[] b4 = new byte[4];
            leis.readFully(b4);
            filename += new String(b4, 0, 4, Charset.forName("UTF-16LE"));
            int p = filename.indexOf(0);
            if (p != -1) {
                filename = filename.substring(0, p);
            }
Debug.println(Level.FINE, "subEntryNo: " + subEntryNo + ", " + isLast + ", " + filename);
        }
        public int compareTo(LongNameDirectoryEntry entry) {
            return this.subEntryNo - entry.subEntryNo;
        }
    }

    /** */
    public interface FileEntry extends DirectoryEntry, Comparable<FileEntry> {
        public boolean isDirectory();
        public String getName();
        int getStartCluster();
        void setLongName(Collection<LongNameDirectoryEntry> longNames);
        public long length();
        long lastModified();
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
    public static class DosDirectoryEntry implements FileEntry {
        /** */
        private static final long serialVersionUID = 1003655836319404523L;
        /** */
        String filename;
        /** */
        int attribute;
        /** */
        int capitalFlag;
        /** unit is 10ms */
        Date created;
        /** unit is date */
        Date lastAccessed;
        /** unit is 2sec */
        Date lastModified;
        /** */
        int startCluster;
        /** */
        long length;
        /** */
        protected String getPrefixString() {
            return "";
        }
        /** */
        DosDirectoryEntry(DataInput leis) throws IOException {
            byte[] b1 = new byte[11];
            leis.readFully(b1);
            filename = getPrefixString() + new String(b1, getPrefixString().length(), 8 - getPrefixString().length()).trim();
            String extention = new String(b1, 8, 3).trim();
            filename += extention.length() > 0 ? '.' + extention : "";
            attribute = leis.readUnsignedByte();
            capitalFlag = leis.readUnsignedByte();
            int lastCreated10msec = leis.readUnsignedByte();
            int lastCreatedTimeDos = leis.readShort();
            int lastCreatedDateDos = leis.readShort();
            created = new Date(DateUtil.dosDateTimeToLong(lastCreatedDateDos, lastCreatedTimeDos) + lastCreated10msec * 10);
            int lastAccessedDateDos = leis.readShort();
            lastAccessed = new Date(DateUtil.dosDateTimeToLong(lastAccessedDateDos, 0));
            int startClusterHigh = leis.readShort();
            int lastModifiedTimeDos = leis.readShort();
            int lastModifiedDateDos = leis.readShort();
            lastModified = new Date(DateUtil.dosDateTimeToLong(lastModifiedDateDos, lastModifiedTimeDos));
            int startClusterLow = leis.readShort() & 0xffff;
            startCluster = (startClusterHigh << 16) | startClusterLow; 
            length = leis.readInt();
        }
        /** */
        public boolean isDirectory() {
            return (attribute & 0x10) != 0;
        }
        /** */
        String longName;
        /** */
        public final void setLongName(Collection<LongNameDirectoryEntry> longNames) {
            StringBuilder sb = new StringBuilder();
            for (LongNameDirectoryEntry entry : longNames) {
Debug.println(Level.FINE, "subEntryNo: " + entry.subEntryNo + ", " + entry.isLast + ", " + entry.filename);
                sb.append(entry.filename);
            }
            longName = sb.toString();
Debug.println(Level.FINE, "longName: " + longName + ", " + longNames.size() + ", " + filename);
        }
        /** */
        public String getName() {
            if (longName != null) {
                return longName;
            } else {
                return filename;
            }
        }
        /** */
        public int getStartCluster() {
            return startCluster;
        }
        /** */
        public int compareTo(FileEntry entry) {
            return getName().compareTo(entry.getName());
        }
        /** */
        public long length() {
            return length;
        }
        /** */
        public long lastModified() {
            return lastModified.getTime();
        }
        /** */
        public long lastAccessed() {
            return lastAccessed.getTime();
        }
        /** */
        public long created() {
            return created.getTime();
        }
        @Override
        public String toString() {
            return getName() + ", " + length() + ", " + LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified()), ZoneId.of("+9"));
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
Debug.println(Level.FINE, "clusters: " + StringUtil.paramString(clusters));
int fcs = (bpb.getFatSectors() / bpb.getSectorsPerCluster());
if (clusters.length > fcs) {
 clusters = Arrays.copyOfRange(clusters, 0, fcs);
 Debug.println(Level.WARNING, "clusters is larger than definitions, shorten: " + clusters.length);
}
            List<LongNameDirectoryEntry> deletedLongNames = new ArrayList<>();
            SortedSet<LongNameDirectoryEntry> tempraryLongNames = new TreeSet<>();
            for (int cluster = 0; cluster < clusters.length; cluster++) {
                for (int sector = 0; sector < bpb.getSectorsPerCluster(); sector++) {
//Debug.println(Level.FINE, "sector: " + (bpb.getSector(clusters[cluster]) + sector));
                    byte[] buffer = new byte[1024]; 
                    io.readSector(buffer, bpb.toSector(clusters[cluster]) + sector);
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
        io.readSector(bytes, 1);
        bpb = new BPB();
        Injector.Util.inject(new ByteArrayInputStream(bytes), bpb);
        fat = bpb.getFatType();
Debug.println("fat: " + fat);
        FatType.class.cast(fat).setRuntimeContext(io, bpb);
    }

    /** */
    public FileAllocationTable(IOSource io, BiosParameterBlock bpb) throws IOException {
        this.io = io;
        this.bpb = bpb;
        fat = bpb.getFatType();
Debug.println("fat: " + fat);
        FatType.class.cast(fat).setRuntimeContext(io, bpb);
    }
}

/* */
