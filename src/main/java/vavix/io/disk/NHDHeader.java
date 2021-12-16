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
 * T98Next HDD (IDE)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/12 umjammer initial version <br>
 */
@Injector(bigEndian = false)
public class NHDHeader implements Sxsi {
    static final String signature = "T98HDDIMAGE.R0";
    @Element(sequence = 1)
    byte[] sig = new byte[16];
    @Element(sequence = 2, value = "0x100")
    String comment;
    @Element(sequence = 3, value = "unsigned int")
    long headersize;
    @Element(sequence = 4, value = "unsigned int")
    long cylinders;
    @Element(sequence = 5, value = "unsigned short")
    int surfaces;
    @Element(sequence = 6, value = "unsigned short")
    int sectors;
    @Element(sequence = 7, value = "unsigned short")
    int sectorsize;
    @Element(sequence = 8)
    byte[] reserved = new byte[0xe2];
    @Override
    public String toString() {
        return String.format(
                "NHDHeader [sig=%s, comment=%s, headersize=%s, cylinders=%s, surfaces=%s, sectors=%s, sectorsize=%s, reserved=%s]",
                Arrays.toString(sig), comment, headersize, cylinders, surfaces, sectors, sectorsize,
                Arrays.toString(reserved));
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
        return (int) headersize;
    }
}
/* */
