/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.util.Arrays;

import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * PC98BiosParameterBlock.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
@Serdes(bigEndian = false)
public class PC98BiosParameterBlock implements BiosParameterBlock {

    @Element(sequence = 1)
    byte[] jump = new byte[3];

    @Element(sequence = 2, value = "8")
    public String oemLavel;

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
    String fileSystem;

    /** */
    public int firstDataSector;

    /** */
    public int countOfClusters;

    /** */
    private FatType type;

    /**
     * do after injection
     *
     * @after #firstDataSector
     * @after #countOfClusters
     * @after #type
     */
    public void compute() {
        //
        int rootDirSectors = ((maxRootDirectoryEntries * 32) + (getBytesPerSector() - 1)) / getBytesPerSector();

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
                "BootRecord [jump=%s, oemLavel=%s, bytesPerSector=%s, sectorsPerCluster=%s, reservedSectors=%s, numberOfFAT=%s, maxRootDirectoryEntries=%s, numberOfSmallSectors=%s, mediaDescriptor=%s, numberOfFATSector=%s, numberOfBIOSSector=%s, numberOfBIOSHeader=%s, invisibleSectors=%s, numberOfLargeSectors=%s, osData=%s, volumeSerialID=%s, volumeLabel=%s, fileSystem=%s]",
                Arrays.toString(jump), oemLavel, bytesPerSector, sectorsPerCluster, reservedSectors, numberOfFAT,
                maxRootDirectoryEntries, numberOfSmallSectors, mediaDescriptor, numberOfFATSector, numberOfBIOSSector,
                numberOfBIOSHeader, invisibleSectors, numberOfLargeSectors, Arrays.toString(osData), volumeSerialID,
                new String(volumeLabel), new String(fileSystem));
    }

    // TODO
    public boolean validate() {
        if (!oemLavel.startsWith("NEC")) {
            return false;
        }
        return true;
    }

    @Override
    public int getSectorsPerCluster() {
        return sectorsPerCluster;
    }

    @Override
    public int getStartClusterOfRootDirectory() {
        return (reservedSectors + numberOfFAT * numberOfFATSector) / sectorsPerCluster;
    }

    @Override
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    @Override
    public int getFatStartSector(int fatNumber) {
//Debug.printf("reservedSectors: %d, fatNumber: %d, numberOfFATSector: %d, result: %d%n", reservedSectors, fatNumber, numberOfFATSector, reservedSectors + fatNumber * numberOfFATSector);
        return reservedSectors + fatNumber * numberOfFATSector;
    }

    @Override
    public int getLastCluster() {
        return (int) ((numberOfLargeSectors + (sectorsPerCluster - 1)) / sectorsPerCluster);
    }

    @Override
    public int toSector(int cluster) {
        int sector = cluster * sectorsPerCluster;
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
