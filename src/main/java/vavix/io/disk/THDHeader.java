/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.disk;

import vavi.util.injection.Element;
import vavi.util.injection.Injector;


/**
 * T98 HDD (IDE)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/12 umjammer initial version <br>
 */
@Injector(bigEndian = false)
public class THDHeader implements Sxsi {
    @Element(sequence = 1, value = "unsigned int")
    int    cylinders;
    @Override
    public String toString() {
        return String.format(
                "THDHeader [cylinders=%s, getTotalSectors()=%s, getSectorSize()=%s, getSectors()=%s, getSurfaces()=%s, getHeaderSize()=%s]",
                cylinders, getTotalSectors(), getSectorSize(), getSectors(), getSurfaces(), getHeaderSize());
    }
    @Override
    public long getTotalSectors() {
        return cylinders * getSectorSize() * getSurfaces();
    }
    @Override
    public long getCylinders() {
        return cylinders;
    }
    @Override
    public int getSectorSize() {
        return 256;
    }
    @Override
    public int getSectors() {
        return 33;
    }
    @Override
    public int getSurfaces() {
        return 8;
    }
    @Override
    public int getHeaderSize() {
        return 256;
    }
}

/* */
