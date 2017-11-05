/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat32;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.StringUtil;
import vavi.util.win32.DateUtil;
import vavix.io.RawIO;


/**
 * Fat32. 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060108 nsano initial version <br>
 */
public class Fat32 implements Serializable {

    /** */
    private static final long serialVersionUID = -3299419883884944569L;

    /** */
    protected transient RawIO rawIO;

    /** */
    protected BPB bpb;

    /** */
    protected Fat fat;

    /** */
    public class BPB implements Serializable {
        /** */
        private static final long serialVersionUID = -1066456664696979810L;
        /** OEM ラベル名 */
        String oemLavel;
        /** 論理セクタサイズ */
        public int bytesPerSector;
        /** 1クラスタ(アロケーションユニット)あたりのセクタ数 */
        public int sectorsPerCluster;
        /** 予約セクタ数 */
        int reservedSectors;
        /** FAT の数 */
        int numberOfFAT;
        /**
         * ルートディレクトリエントリの最大数
         * FAT12/16 用であり，FAT32 では常に 0000h
         */
        int maxRootDirectoryEntries;
        /**
         * 全セクタ数(Small Sector)
         * FAT32 では常に 0000h 。全セクタ数が WORD(2バイト) で表される場合のみ
         */
        int numberOfSmallSectors;
        /** メディアディスクリプタ */
        int mediaDescriptor;
        /** */
        int numberOfFATSector;
        /** */
        int sectorsPerTrack;
        /** */
        int headsPerDrive;
        /** */
        int invisibleSectors;
        /** */
        public long numberOfLargeSectors;
        /** */
        int sectorsPerFAT;
        /** */
        int mediaDescriptionFlag;
        /** */
        int fileSystemVersion;
        /** */
        int startClusterOfRootDirectory;
        /** */
        int sectorOfFSInfo;
        /** */
        int sectorOfCopyBootSector;
        /** */
        int physicalDriveNumber;
        /** */
        int bootSignature;
        /** */
        int volumeSerialID;
        /** */
        String volumeLavel;
        /** */
        String fileSystemType;
        /** */
        BPB() throws IOException {
            byte[] buffer = rawIO.readSector(0);
            InputStream is = new ByteArrayInputStream(buffer);
            @SuppressWarnings("resource")
            LittleEndianDataInputStream leis = new LittleEndianDataInputStream(is);
            byte[] b1 = new byte[3];
            leis.readFully(b1);
            byte[] b2 = new byte[8];
            leis.readFully(b2);
            oemLavel = new String(b2);
            bytesPerSector = leis.readShort();
            sectorsPerCluster = leis.readUnsignedByte();
            reservedSectors = leis.readShort();
            numberOfFAT = leis.readUnsignedByte();
            maxRootDirectoryEntries = leis.readShort();
            numberOfSmallSectors = leis.readShort();
            mediaDescriptor = leis.readUnsignedByte();
            numberOfFATSector = leis.readShort();
            sectorsPerTrack = leis.readShort();
            headsPerDrive = leis.readShort();
            invisibleSectors = leis.readInt();
            numberOfLargeSectors = leis.readInt();
            sectorsPerFAT = leis.readInt();
            mediaDescriptionFlag = leis.readShort();
            fileSystemVersion = leis.readShort();
            startClusterOfRootDirectory = leis.readInt();
            sectorOfFSInfo = leis.readShort();
            sectorOfCopyBootSector = leis.readShort();
            byte[] b3 = new byte[12];
            leis.readFully(b3);
            physicalDriveNumber = leis.readUnsignedByte();
            byte[] b4 = new byte[1];
            leis.readFully(b4);
            bootSignature = leis.readUnsignedByte();
            volumeSerialID = leis.readInt();
            byte[] b5 = new byte[11];
            leis.readFully(b5);
            volumeLavel = new String(b5);
            byte[] b6 = new byte[8];
            leis.readFully(b6);
            fileSystemType = new String(b6);
//System.err.println(StringUtil.paramString(this).replace(',', '\n'));
        }
        public final int getBytesPerCluster() {
            return bytesPerSector * sectorsPerCluster;
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
    class UserFat implements Fat {
        /** */
        private static final long serialVersionUID = -6771019237363727447L;
        /** */
        int[] clusters;
        /** */
        UserFat() throws IOException {
            int size = getLastCluster();
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
            } while (00000002 <= cluster && cluster <= 0x0ffffff6);
            return clusters.toArray(new Integer[clusters.size()]);
        }
        /** */
        public boolean isUsing(int cluster) throws IOException {
            int value = getClusterValue(cluster);
            return 00000002 <= value && value <= 0x0fffffff;
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
                    fat.setClusterValue(clusters[i], 0x0fffffff);
                } else {
                    fat.setClusterValue(clusters[i], clusters[i + 1]);
                }
            }
        }
    }

    /** */
    class Fat32Fat implements Fat {
        /** */
        private static final long serialVersionUID = -8008307331300948383L;
        /** */
        int fatNumber = 0;
        /** */
        public void setFatNumber(int fatNumber) {
            this.fatNumber = fatNumber;
        }
        /** */
        private int getStartSector() {
             return bpb.reservedSectors + fatNumber * bpb.sectorsPerFAT;
        }

        /** TODO thread unsafe */
        private byte[] buffer;
        /** TODO thread unsafe */
        private int currentSector;
        /** TODO thread unsafe */
        int nextCluster(int cluster) throws IOException {
            int sector = getStartSector() + cluster * 4 / bpb.bytesPerSector;
            if (sector != currentSector) {
                buffer = rawIO.readSector(sector);
                currentSector = sector;
            }
            int position = (cluster * 4) % bpb.bytesPerSector;
            LittleEndianDataInputStream leis = new LittleEndianDataInputStream(new ByteArrayInputStream(buffer, position, 4));
            int nextCluster = leis.readInt();
            leis.close();
//System.err.println("sector: " + currentSector + ", posision: " + position + ", " + StringUtil.getDump(buffer, position, 4));
            return nextCluster;
        }

        /**
         * @param cluster startCluster
         */
        public Integer[] getClusterChain(int cluster) throws IOException {
            List<Integer> clusters = new ArrayList<>();
            do {
//System.err.println("cluster: " + StringUtil.toHex8(cluster));
                clusters.add(cluster);
                cluster = nextCluster(cluster);
            } while (00000002 <= cluster && cluster <= 0x0ffffff6);
            return clusters.toArray(new Integer[clusters.size()]);
        }

        /**
         * <li>00000000h 未使用クラスタ 
         * <li>00000002h 〜 0ffffff6h ファイルの次のクラスタ 
         * <li>0ffffff7h 不良クラスタ 
         * <li>0ffffff8h 〜 0fffffffh ファイルの最終クラスタ(通常は 0fffffffh が使用される) 
         * @param cluster cluster
         */
        public boolean isUsing(int cluster) throws IOException {
            cluster = nextCluster(cluster);
//System.err.println("cluster: " + StringUtil.toHex8(cluster));
            return 00000002 <= cluster && cluster <= 0x0fffffff;
        }
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
    class DeletedLongNameDirectoryEntry extends LongNameDirectoryEntry {
        /** */
        private static final long serialVersionUID = -3509895194089903860L;

        DeletedLongNameDirectoryEntry(InputStream is) throws IOException {
            super(is);
            subEntryNo = -1;
            isLast = false;
        }
    }

    /** */
    class LongNameDirectoryEntry implements DirectoryEntry, Comparable<LongNameDirectoryEntry> {
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
        LongNameDirectoryEntry(InputStream is) throws IOException {
            @SuppressWarnings("resource")
            LittleEndianDataInputStream leis = new LittleEndianDataInputStream(is);
            int sequenceByte = leis.readUnsignedByte();
            subEntryNo = sequenceByte & 0x3f;
            isLast = (sequenceByte & 0x40) != 0;
            byte[] b1 = new byte[10];
            leis.readFully(b1);
            filename = new String(b1, 0, 10, "UTF-16LE");
            attribute = leis.readUnsignedByte(); 
            leis.readUnsignedByte(); // longEntryType
            shortNameCheckSum = leis.readUnsignedByte();
            byte[] b2 = new byte[12];
            leis.readFully(b2);
            filename += new String(b2, 0, 12, "UTF-16LE");
            byte[] b3 = new byte[2];
            leis.readFully(b3);
            byte[] b4 = new byte[4];
            leis.readFully(b4);
            filename += new String(b4, 0, 4, "UTF-16LE");
            int p = filename.indexOf(0);
            if (p != -1) {
                filename = filename.substring(0, p);
            }
//System.err.println("subEntryNo: " + subEntryNo + ", " + isLast + ", " + filename);
        }
        public int compareTo(LongNameDirectoryEntry entry) {
            return this.subEntryNo - entry.subEntryNo;
        }
    }

    /** */
    public interface FileEntry extends DirectoryEntry, Comparable<FileEntry> {
        boolean isDirectory();
        String getName();
        int getStartCluster();
        void setLongName(Collection<LongNameDirectoryEntry> longNames);
        long length();
        long lastModified();
    }

    /**
     * 
     * {@link DosDirectoryEntry#lastAccessed()} は削除した日付
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
        DeletedDirectoryEntry(InputStream is) throws IOException {
            super(is);
        }
        /**
         * startClusterHigh を見つける
         * @return false if not found
         */
        public boolean resolveStartCluster(MatchingStrategy<byte[], ?> matching) throws IOException {

            int startClusterHigh = -1;

            byte[] buffer = new byte[bpb.bytesPerSector];
            for (int i = 0; i < (getLastCluster() + 0xffff) / 0x10000; i++) {
                int startCluster = i * 0x10000 + this.startCluster;
                int targetSector = getSector(startCluster);
                readSector(buffer, targetSector);
                Matcher<MatchingStrategy<byte[], ?>> matcher = new StrategyPatternMatcher<>(buffer);
                if (matcher.indexOf(matching, 0) != -1) {

                    // 使われていたら次

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
        /** */
//        public abstract class FindingStrategy {
//            public abstract List<Integer> getClusterList(int startCluster);
//        }
    }

    /** */
    public class DosDirectoryEntry implements FileEntry {
        /** */
        private static final long serialVersionUID = 1003655836319404523L;
        /** */
        String filename;
        /** */
        int attribute;
        /** */
        int capitalFlag;
        /** 10ms 単位 */
        Date created;
        /** 日付単位 */
        Date lastAccessed;
        /** 2sec 単位 */
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
        DosDirectoryEntry(InputStream is) throws IOException {
            @SuppressWarnings("resource")
            LittleEndianDataInputStream leis = new LittleEndianDataInputStream(is);
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
//System.err.println("subEntryNo: " + entry.subEntryNo + ", " + entry.isLast + ", " + entry.filename);
                sb.append(entry.filename);
            }
            longName = sb.toString();
//System.err.println("longName: " + longName + ", " + longNames.size() + ", " + filename);
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
    }

    /** */
    class Directory {
        /** */
        Map<String, FileEntry> entries;
        /**
         * @param path F:\xxx\yyy
         */
        Directory(String path) throws IOException {
//System.err.println("**** directory: \\");
            if (path.indexOf(':') == 1) {
                path = path.substring(2);
            }
            entries = getEntries(bpb.startClusterOfRootDirectory);
            StringTokenizer st = new StringTokenizer(path, "\\");
            while (st.hasMoreTokens()) {
                String directory = st.nextToken();
//System.err.println("**** directory: " + directory);
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
//System.err.println("clusters: " + StringUtil.paramString(clusters));
            List<LongNameDirectoryEntry> deletedLongNames = new ArrayList<>();
            SortedSet<LongNameDirectoryEntry> tempraryLongNames = new TreeSet<>();
            for (int cluster = 0; cluster < clusters.length; cluster++) {
                for (int sector = 0; sector < bpb.sectorsPerCluster; sector++) {
//System.err.println("sector: " + (getSector(clusters[cluster]) + sector));
                    byte[] buffer = rawIO.readSector(getSector(clusters[cluster]) + sector);
                    for (int entry = 0; entry < 16; entry++) {
                        InputStream is = new ByteArrayInputStream(buffer, entry * 32, 32);
                        int firstByte = buffer[entry * 32] & 0xff;
                        int attributeByte = buffer[entry * 32 + 0x0b];
                        switch (firstByte) {
                        case 0x00:
//System.err.println("none");
                            break;
                        case 0xe5: {
                            if (attributeByte == 0x0f) {
                                LongNameDirectoryEntry directoryEntry = new DeletedLongNameDirectoryEntry(is);
                                deletedLongNames.add(0, directoryEntry);
                            } else {
                                FileEntry directoryEntry = new DeletedDirectoryEntry(is);
//System.err.println(StringUtil.paramString(directoryEntry));
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
                                LongNameDirectoryEntry directoryEntry = new LongNameDirectoryEntry(is);
//System.err.println(StringUtil.paramString(directoryEntry));
                                tempraryLongNames.add(directoryEntry);
                            } else {
                                FileEntry directoryEntry = new DosDirectoryEntry(is);
//System.err.println(StringUtil.paramString(directoryEntry));
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
// System.err.println(name + ": " + entries.get(name).getStartCluster() + ", " + StringUtil.toHex8(entries.get(name).getStartCluster()));
//}
            return entries;
        }
    }

    /** */
    public final int getSector(int cluster) {
        int sector = (cluster - 2) * bpb.sectorsPerCluster + bpb.reservedSectors + bpb.sectorsPerFAT * bpb.numberOfFAT;
        return sector;
    }

    /** */
    public final int getLastCluster() throws IOException {
        return (int) ((bpb.numberOfLargeSectors + (bpb.sectorsPerCluster - 1)) / bpb.sectorsPerCluster);
    }

    /** */
    public final int getBytesPerCluster() {
        return bpb.getBytesPerCluster();
    }

    /** */
    public final int getBytesPerSector() {
        return bpb.bytesPerSector;
    }

    /** */
    public final int getSectorsPerCluster() {
        return bpb.sectorsPerCluster;
    }

    /** */
    public int getRequiredClusters(long size) {
        return (int) ((size + (getBytesPerCluster() - 1)) / getBytesPerCluster());
    }

    /** */
    public void setFatNumber(int fatNumber) {
        if (fat instanceof Fat32Fat) {
            ((Fat32Fat) fat).setFatNumber(fatNumber);
        } else {
            throw new UnsupportedOperationException("current fat is not support fat number");
        }
    }

    /** {@link #fat} set to UserFat */
    public Fat useUserFat() throws IOException {
        this.fat = new UserFat();
        return fat;
    }

    /** TODO naming */
    public final boolean isUsing(int cluster) throws IOException {
        return fat.isUsing(cluster);
    }

    /** */
    public int readCluster(byte[] buffer, int cluster) throws IOException {
        byte[] buf = new byte[bpb.bytesPerSector];
        for (int sector = 0; sector < bpb.sectorsPerCluster; sector++) {
            int targetSector = getSector(cluster) + sector;
            rawIO.readSector(buf, targetSector);
            System.arraycopy(buf, 0, buffer, bpb.bytesPerSector * sector, bpb.bytesPerSector);
        }
        return bpb.getBytesPerCluster();
    }

    /** */
    public int readSector(byte[] buffer, int sector) throws IOException {
        return rawIO.readSector(buffer, sector);
    }

    /** */
    public Map<String, FileEntry> getEntries(String path) throws IOException {
        Directory directory = new Directory(path);
        return directory.entries;
    }

    /** */
    public Fat32(String deviceName) throws IOException {
        deviceName = deviceName.toUpperCase();
        if (!deviceName.matches("[A-Z]\\:")) {
            throw new IllegalArgumentException(deviceName);
        }
        rawIO = new RawIO("\\\\.\\" + deviceName);
        bpb = new BPB();
        fat = new Fat32Fat();
    }

    /** */
    public interface MatchingStrategy<S, P> {
        int indexOf(S source, P pattern);
    }

    /** */
    class StrategyPatternMatcher<S, P extends MatchingStrategy<S, ?>> implements Matcher<P> {
        S source;
        StrategyPatternMatcher(S source) {
            this.source = source;
        }
        public int indexOf(P matchingStrategy, int fromIndex) {
            return matchingStrategy.indexOf(source, null);
        }
    }

    /** */
    public interface Matcher<T> {
        int indexOf(T pattern, int fromIndex);
    }

    /** */
    public Matcher<byte[]> matcher(byte[] buffer) {
        return new ByteArrayMatcher(buffer);
    }

    /** */
    class ByteArrayMatcher implements Matcher<byte[]> {
        byte[] source;
        public ByteArrayMatcher(byte[] buffer) {
            this.source = buffer;
        }
        /** Boyer-Moore 法 */
        public int indexOf(byte[] pattern, int fromIndex) {
            int[] skipTable = new int[256];
            if (fromIndex >= source.length) {
                return -1;
            }
            for (int ch = 0; ch < skipTable.length; ch++) {
                skipTable[ch] = pattern.length;
            }
            for (int ch = 0; ch < (pattern.length - 1); ch++) {
                skipTable[pattern[ch] & 0xff] = pattern.length - 1 - ch;
            }
            int i = fromIndex + pattern.length - 1;
            while (i < source.length) {
                if (source[i] == pattern[pattern.length - 1]) {
                    int j = i;
                    int k = pattern.length - 1;
                    while (source[--j] == pattern[--k]) {
                        if (k == 0) {
                            return j;
                        }
                    }
                }
                i = i + skipTable[source[i] & 0xff];
            }
            return -1;
        }
    }

    //----

    /**
     * @param args 0:dir F:\xxx\yyy
     */
    public static void main(String[] args) throws Exception {
        String path = args[0];
        String deviceName = path.substring(0, 2);
//System.err.println(deviceName + ", " + path);
        Fat32 fat32 = new Fat32(deviceName);
        Map<String, FileEntry> entries = fat32.getEntries(path);
for (FileEntry entry : entries.values()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
    }
}

/* */
