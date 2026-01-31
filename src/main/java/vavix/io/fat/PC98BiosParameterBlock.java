/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.util.Arrays;

import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;

import static java.lang.System.getLogger;


/**
 * PC98BiosParameterBlock.
 * <p>
 * system property
 * <li>{@code vavix.io.fat.PC98BiosParameterBlock.validation} ... {@code class#method}, {@code true}, {@code false}, default {@code false}</li>
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
@Serdes(bigEndian = false, encoding = "MS932")
public class PC98BiosParameterBlock implements BiosParameterBlock {

    private static final Logger logger = getLogger(PC98BiosParameterBlock.class.getName());

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

    /**
     * true: do default validation,
     * false: no validation,
     * else: validation function name "class#method", the method must return boolean and
     *       w/ an argument PC98BiosParameterBlock and static.
     */
    public static final String VALIDATION_KEY = "vavix.io.fat.PC98BiosParameterBlock.validation";

    /** @see #VALIDATION_KEY */
    public boolean validate() {
        String validation = System.getProperty(VALIDATION_KEY, "true");
        if (Boolean.parseBoolean(validation)) {
logger.log(Level.DEBUG, "default validation");
            return this.fileSystem.contains("FAT");
        } else if (validation.equalsIgnoreCase("false")) {
logger.log(Level.DEBUG, "no validation, accepting anyway");
            return true;
        } else {
            try {
                String[] parts = validation.split("#");
                Class<?> clazz = Class.forName(parts[0]);
                Method method = clazz.getDeclaredMethod(parts[1], PC98BiosParameterBlock.class);
                if (method.getReturnType() != Boolean.TYPE) {
                    throw new IllegalArgumentException("method %s return type is not boolean but %s".formatted(method.getName(), method.getReturnType().getName()));
                }
                boolean r = method.invoke(null, this).equals(Boolean.TRUE);
logger.log(Level.DEBUG, "do user bpb validation %s#%s: %s".formatted(clazz.getSimpleName(), method.getName(), r));
                return r;
            } catch (Exception e) {
logger.log(Level.WARNING, "validation function error, accepting anyway", e);
                return true;
            }
        }
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
logger.log(Level.TRACE, "reservedSectors: %d, fatNumber: %d, numberOfFATSector: %d, result: %d".formatted(reservedSectors, fatNumber, numberOfFATSector, reservedSectors + fatNumber * numberOfFATSector));
        return reservedSectors + fatNumber * numberOfFATSector;
    }

    @Override
    public int getLastCluster() {
        return (numberOfLargeSectors + (sectorsPerCluster - 1)) / sectorsPerCluster;
    }

    // TODO same as the AT's
    @Override
    public int toSector(int cluster) {
        int sector = switch (type) {
            default -> (cluster - 2) * sectorsPerCluster + firstDataSector;
            case Fat16Fat, Fat12Fat ->
                    cluster == 0 ? firstDataSector : firstDataSector + rootDirSectors + (cluster - 2) * sectorsPerCluster;
        };
logger.log(Level.DEBUG, "cluster: %d -> sector: %d, firstDataSector: %d, rootDirSectors: %d, sectorsPerCluster: %d, bytesPerSector: %d, distinguish root threshold: %d".formatted(cluster, sector, firstDataSector, rootDirSectors, sectorsPerCluster, bytesPerSector, rootDirSectors / sectorsPerCluster));
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

    /**
     * @see "https://github.com/aaru-dps/Aaru.Helpers/blob/4640bb88d3eb907d0f0617d5ee5159fbc13c5653/CHS.cs"
     */
    public static int toLBA(int cyl, int head, int sector, int maxHead, int maxSector) {
//logger.log(Level.DEBUG, "heads: %d, secs: %d".formatted(maxHead, maxSector));
        return maxHead == 0 || maxSector == 0 ? (((cyl * 16)      + head) * 63)        + sector - 1
                                              : (((cyl * maxHead) + head) * maxSector) + sector - 1;
    }
}
