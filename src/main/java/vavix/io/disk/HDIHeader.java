/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.disk;

import java.util.Arrays;

import vavi.util.injection.Element;
import vavi.util.injection.Injector;


/**
 * ANEX86 HDD (SASI)
 * @author Mamiya
 */
@Injector(bigEndian = false)
public class HDIHeader implements Sxsi {
    @Element(sequence = 1)
    byte[] dummy = new byte[4];
    @Element(sequence = 2)
    int hddtype;
    @Element(sequence = 3)
    int headersize;
    @Element(sequence = 4)
    int hddsize;
    @Element(sequence = 5)
    int sectorsize;
    @Element(sequence = 6)
    int sectors;
    @Element(sequence = 7)
    int surfaces;
    @Element(sequence = 8)
    int cylinders;
    @Override
    public String toString() {
        return String.format(
                "HDIHeader [dummy=%s, hddtype=%s, headersize=%s, hddsize=%s, sectorsize=%s, sectors=%s, surfaces=%s, cylinders=%s]",
                Arrays.toString(dummy), hddtype, headersize, hddsize, sectorsize, sectors, surfaces, cylinders);
    }
    @Override
    public long getTotalSectors() {
        return cylinders * sectors * surfaces;
    }
    @Override
    public long getCylinders() {
        return cylinders;
    }
    @Override
    public int getSectorSize() {
        return sectorsize;
    }
    @Override
    public int getSectors() {
        return sectors;
    }
    @Override
    public int getSurfaces() {
        return surfaces;
    }
    @Override
    public int getHeaderSize() {
        return headersize;
    }
}

/* */
