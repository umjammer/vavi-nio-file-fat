/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

/**
 * BiosParameterBlock.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
public interface BiosParameterBlock {

    int getSectorsPerCluster();

    int getStartClusterOfRootDirectory();

    int getBytesPerSector();

    int getFatStartSector(int fatNumber);

    /** for deleted, or write */
    int getLastCluster();

    /** converts cluster number to sector number */
    int toSector(int cluster);

    int getFatSectors();

    FatType getFatType();
}
