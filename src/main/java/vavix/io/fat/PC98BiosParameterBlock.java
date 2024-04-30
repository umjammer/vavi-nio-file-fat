/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.util.Arrays;
import java.util.logging.Level;

import vavi.util.Debug;
import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * PC98BiosParameterBlock.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
@Serdes(bigEndian = false, encoding = "MS932")
public class PC98BiosParameterBlock implements BiosParameterBlock {

    @Element(sequence = 1)
    byte[] jump = new byte[3];

    @Element(sequence = 2, value = "8")
    public String oemLabel;

    @Element(sequence = 3, value = "unsigned short")
    int bytesPerSector;
    @Element(sequence = 4, value = "unsigned byte")
    int sectorsPerCluster;
    @Element(sequence = 5, value = "unsigned short")
    public int reservedSectors;
    @Element(sequence = 6, value = "unsigned byte")
    public int numberOfFAT;
    @Element(sequence = 7, value = "unsigned short")
    public int maxRootDirectoryEntries;
    @Element(sequence = 8, value = "unsigned short")
    public int numberOfSmallSectors;
    @Element(sequence = 9, value = "unsigned byte")
    public int mediaDescriptor;
    @Element(sequence = 10, value = "unsigned short")
    public int numberOfFATSector;
    @Element(sequence = 11, value = "unsigned short")
    int numberOfBIOSSector;
    @Element(sequence = 12, value = "unsigned short")
    int numberOfBIOSHeader;
    @Element(sequence = 13)
    int invisibleSectors;
    @Element(sequence = 14)
    public int numberOfLargeSectors;

    @Element(sequence = 15)
    byte[] osData = new byte[3];
    @Element(sequence = 16)
    public int volumeSerialID;
    @Element(sequence = 17, value = "11")
    public String volumeLabel;
    @Element(sequence = 18, value = "8")
    public String fileSystem;

    /** */
    public int firstDataSector;

    /** */
    public int countOfClusters;

    /** */
    private FatType type;

    /** */
    public int rootDirSectors;

    /**
     * do after injection
     *
     * @after #firstDataSector
     * @after #countOfClusters
     * @after #type
     */
    public void compute() {
        rootDirSectors = ((maxRootDirectoryEntries * 32) + (getBytesPerSector() - 1)) / getBytesPerSector();

        int totalSectors;
        if (numberOfSmallSectors != 0)
            totalSectors = numberOfSmallSectors;
        else
            totalSectors = numberOfLargeSectors;

        int dataSectors = totalSectors - (reservedSectors + (numberOfFAT * numberOfFATSector) + rootDirSectors);

        countOfClusters = dataSectors / getSectorsPerCluster();

        if (countOfClusters < 4085)
            type = FatType.Fat12Fat;
        else if (countOfClusters < 65525)
            type = FatType.Fat16Fat;
        else
            type = FatType.Fat32Fat;

        switch (getFatType()) {
        case Fat32Fat:
            firstDataSector = reservedSectors + (numberOfFAT * numberOfFATSector) + rootDirSectors;
            break;
        default:
            firstDataSector = reservedSectors + (numberOfFAT * numberOfFATSector);
            break;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "BootRecord [jump=%s, oemLabel=%s, bytesPerSector=%s, sectorsPerCluster=%s, reservedSectors=%s, numberOfFAT=%s, maxRootDirectoryEntries=%s, numberOfSmallSectors=%s, mediaDescriptor=%s, numberOfFATSector=%s, numberOfBIOSSector=%s, numberOfBIOSHeader=%s, invisibleSectors=%s, numberOfLargeSectors=%s, osData=%s, volumeSerialID=%s, volumeLabel=%s, fileSystem=%s]",
                Arrays.toString(jump), oemLabel, bytesPerSector, sectorsPerCluster, reservedSectors, numberOfFAT,
                maxRootDirectoryEntries, numberOfSmallSectors, mediaDescriptor, numberOfFATSector, numberOfBIOSSector,
                numberOfBIOSHeader, invisibleSectors, numberOfLargeSectors, Arrays.toString(osData), volumeSerialID,
                volumeLabel, fileSystem);
    }

    // TODO
    public boolean validate() {
//        if (!oemLabel.startsWith("NEC")) {
//            return false;
//        }
Debug.println(Level.FINE, "oemLabel: " + oemLabel);
        return true;
    }

    @Override
    public int getSectorsPerCluster() {
        return sectorsPerCluster;
    }

    @Override
    public int getStartClusterOfRootDirectory() {
        return 0;
    }

    @Override
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    @Override
    public int getFatStartSector(int fatNumber) {
Debug.printf(Level.FINER, "reservedSectors: %d, fatNumber: %d, numberOfFATSector: %d, result: %d%n", reservedSectors, fatNumber, numberOfFATSector, reservedSectors + fatNumber * numberOfFATSector);
        return reservedSectors + fatNumber * numberOfFATSector;
    }

    @Override
    public int getLastCluster() {
        return (numberOfLargeSectors + (sectorsPerCluster - 1)) / sectorsPerCluster;
    }

    // TODO same as the AT's
    @Override
    public int toSector(int cluster) {
        int sector;
        switch (type) {
        case Fat32Fat:
        default:
            sector = (cluster - 2) * sectorsPerCluster + firstDataSector;
            break;
        case Fat16Fat:
        case Fat12Fat:
            sector = cluster == 0 ? firstDataSector : firstDataSector + rootDirSectors + (cluster - 2) * sectorsPerCluster;
            break;
        }
Debug.printf(Level.FINE, "cluster: %d -> sector: %d, firstDataSector: %d, rootDirSectors: %d, sectorsPerCluster: %d, bytesPerSector: %d, distinguish root threshold: %d", cluster, sector, firstDataSector, rootDirSectors, sectorsPerCluster, bytesPerSector, rootDirSectors / sectorsPerCluster);
        return sector;
    }

    @Override
    public int getFatSectors() {
        return numberOfFATSector;
    }

    /**
     * @before {@link #compute()}
     * @throws IllegalStateException {@link #compute()} has not been called
     */
    @Override
    public FatType getFatType() {
        if (type == null) {
            throw new IllegalStateException("call #compute() first");
        }
        return type;
    }
}

/* */
