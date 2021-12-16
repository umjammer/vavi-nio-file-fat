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
 * Virtual98 HDD (SCSI)
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/12 umjammer initial version <br>
 */
@Injector(bigEndian = false)
public class VHDHeader implements Sxsi {
    static final String signature = "VHD1.00";
    @Element(sequence = 1)
    byte[] sig = new byte[3];
    @Element(sequence = 2, value = "unsigined byte")
    int ver;
    @Element(sequence = 3)
    byte delimita;
    @Element(sequence = 4, value = "128")
    String comment;
    @Element(sequence = 5)
    byte[] padding1 = new byte[4];
    @Element(sequence = 6, value = "unsigined short")
    int mbsize;
    @Element(sequence = 7, value = "unsigined short")
    int sectorsize;
    @Element(sequence = 8)
    int sectors;
    @Element(sequence = 9)
    int surfaces;
    @Element(sequence = 10, value = "unsigined short")
    int cylinders;
    @Element(sequence = 11)
    int totals;
    @Element(sequence = 12)
    byte[] padding2 = new byte[0x44];
    @Override
    public String toString() {
        return String.format(
                "VHDHeader [sig=%s, ver=%s, delimita=%s, comment=%s, padding1=%s, mbsize=%s, sectorsize=%s, sectors=%s, surfaces=%s, cylinders=%s, totals=%s, padding2=%s]",
                Arrays.toString(sig), ver, delimita, comment, Arrays.toString(padding1), mbsize, sectorsize, sectors,
                surfaces, cylinders, totals, Arrays.toString(padding2));
    }
    @Override
    public long getTotalSectors() {
        return totals;
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
        return 0;
    }
}

/* */
