/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;

import vavi.util.Debug;
import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * ATBiosParameterBlock.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
@Serdes(bigEndian = false)
public class ATBiosParameterBlock implements BiosParameterBlock, Serializable {
    /** */
    @Serial
    private static final long serialVersionUID = -1066456664696979810L;
    @Element(sequence = 1)
    byte[] jump = new byte[3];
    /** OEM name */
    @Element(sequence = 2, value = "8")
    String oemLabel;

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
    public int numberOfLargeSectors;

    /** */
    @Element(sequence = 15, condition = "condition1")
    int sectorsPerFAT;

    @Element(sequence = 16, condition = "condition2")
    ATBiosParameterBlock.SubForFat32 fat32;

    @Element(sequence = 17, condition = "condition2")
    ATBiosParameterBlock.SubForFat fat;

    @Serdes(bigEndian = false)
    public static class SubForFat {
        @Element(sequence = 1, value = "unsigned byte")
        int driveNumber;
        @Element(sequence = 2, value = "unsigned byte")
        int b1;
        @Element(sequence = 3, value = "unsigned byte")
        int bootSignature;
        @Element(sequence = 4)
        int volumeSerialID;
        @Element(sequence = 5, value = "11")
        String volumeLabel;
        @Element(sequence = 6, value = "8")
        String fileSystemType;
        @Override
        public String toString() {
            return String.format("Fat [driveNumber=%s, b1=%s, bootSignature=%s, volumeSerialID=%s, volumeLabel=%s, fileSystemType=%s]",
                            driveNumber, b1, bootSignature, volumeSerialID, volumeLabel, fileSystemType);
        }
    }

    @Serdes(bigEndian = false)
    public static class SubForFat32 {
        /** */
        @Element(sequence = 1, value = "unsigned short")
        int mediaDescriptionFlag;
        /** */
        @Element(sequence = 2, value = "unsigned short")
        int fileSystemVersion;
        /** the ROOT directory */
        @Element(sequence = 3)
        int startClusterOfRootDirectory;
        /** */
        @Element(sequence = 4, value = "unsigned short")
        int sectorOfFSInfo;
        /** */
        @Element(sequence = 5, value = "unsigned short")
        int sectorOfCopyBootSector;
        @Element(sequence = 6)
        byte[] b3 = new byte[12];
        /** */
        @Element(sequence = 7, value = "unsigned byte")
        int physicalDriveNumber;
        @Element(sequence = 8)
        byte b4;
        /** */
        @Element(sequence = 9, value = "unsigned byte")
        int bootSignature;
        /** */
        @Element(sequence = 10)
        int volumeSerialID;
        /** */
        @Element(sequence = 11, value = "11")
        String volumeLavel;
        /** */
        @Element(sequence = 12, value = "8")
        String fileSystemType;
        @Override
        public String toString() {
            return String.format("Fat32 [mediaDescriptionFlag=%s, fileSystemVersion=%s, startClusterOfRootDirectory=%s, " +
                            "sectorOfFSInfo=%s, sectorOfCopyBootSector=%s, b3=%s, physicalDriveNumber=%s, b4=%s, bootSignature=%s, " +
                            "volumeSerialID=%s, volumeLabel=%s, fileSystemType=%s]",
                            mediaDescriptionFlag, fileSystemVersion, startClusterOfRootDirectory, sectorOfFSInfo,
                            sectorOfCopyBootSector, Arrays.toString(b3), physicalDriveNumber, b4,
                            bootSignature, volumeSerialID, volumeLavel, fileSystemType);
        }
    }

    public boolean condition1(int sequence) {
        return numberOfFATSector == 0;
    }

    private int fatSize;

    private int firstDataSector;

    private int rootDirSectors;

    /** using jnode algorithm */
    public boolean condition2(int sequence) {
        rootDirSectors = ((maxRootDirectoryEntries * 32) + (bytesPerSector - 1)) / bytesPerSector;

        if (numberOfFATSector != 0)
            fatSize = numberOfFATSector;
        else
            fatSize = sectorsPerFAT;

        int totalSectors;
        if (numberOfSmallSectors != 0)
            totalSectors = numberOfSmallSectors;
        else
            totalSectors = numberOfLargeSectors;

        int dataSectors = totalSectors - (reservedSectors + (numberOfFAT * fatSize) + rootDirSectors);

        int countOfClusters = dataSectors / sectorsPerCluster;

        if (countOfClusters < 4085)
            type = FatType.Fat12Fat;
        else if (countOfClusters < 65525)
            type = FatType.Fat16Fat;
        else
            type = FatType.Fat32Fat;

        switch (type) {
        case Fat32Fat:
            firstDataSector = reservedSectors + (numberOfFAT * fatSize) + rootDirSectors;
            break;
        default:
            firstDataSector = reservedSectors + (numberOfFAT * fatSize);
            break;
        }

        return switch (sequence) {
            case 16 -> type == FatType.Fat32Fat;
            case 17 -> type != FatType.Fat32Fat;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return String.format("BPB [jump=%s, oemLabel=%s, bytesPerSector=%s, sectorsPerCluster=%s, reservedSectors=%s, " +
                        "numberOfFAT=%s, maxRootDirectoryEntries=%s, numberOfSmallSectors=%s, mediaDescriptor=%s, numberOfFATSector=%s, " +
                        "sectorsPerTrack=%s, headsPerDrive=%s, invisibleSectors=%s, numberOfLargeSectors=%s, sectorsPerFAT=%s, %s",
                        Arrays.toString(jump), oemLabel, bytesPerSector,
                        sectorsPerCluster, reservedSectors, numberOfFAT,
                        maxRootDirectoryEntries, numberOfSmallSectors, mediaDescriptor,
                        numberOfFATSector, sectorsPerTrack, headsPerDrive,
                        invisibleSectors, numberOfLargeSectors, sectorsPerFAT,
                        type == FatType.Fat32Fat ? fat32 : fat);
    }

    @Override
    public int getSectorsPerCluster() {
        return sectorsPerCluster;
    }

    @Override
    public int getLastCluster() {
        return (numberOfLargeSectors + (sectorsPerCluster - 1)) / sectorsPerCluster;
    }

    @Override
    public int getFatStartSector(int fatNumber) {
        return reservedSectors + fatNumber * sectorsPerFAT;
    }

    @Override
    public int getStartClusterOfRootDirectory() {
        return type == FatType.Fat32Fat ? fat32.startClusterOfRootDirectory : 0;
    }

    @Override
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    /** using jnode algorithm */
    @Override
    public int toSector(int cluster) {
        int sector = switch (type) {
            default -> (cluster - 2) * sectorsPerCluster + firstDataSector;
            case Fat16Fat, Fat12Fat ->
                    cluster == 0 ? firstDataSector : firstDataSector + rootDirSectors + (cluster - 2) * sectorsPerCluster;
        };
        Debug.printf(Level.FINE, "cluster: %d -> sector: %d, firstDataSector: %d, rootDirSectors: %d, sectorsPerCluster: %d, bytesPerSector: %d", cluster, sector, firstDataSector, rootDirSectors, sectorsPerCluster, bytesPerSector);
        return sector;
    }

    @Override
    public int getFatSectors() {
        return fatSize;
    }

    private FatType type;

    @Override
    public FatType getFatType() {
        return type;
    }
}
