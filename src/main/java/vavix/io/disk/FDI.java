/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.disk;

import vavi.util.injection.Element;
import vavi.util.injection.Injector;


/**
 * FDI.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/07 umjammer initial version <br>
 */
public class FDI {

    @Injector(bigEndian = false)
    static class Header {
        @Element(sequence = 1)
        byte[] dummy = new byte[4];
        @Element(sequence = 2)
        int fddtype;
        @Element(sequence = 3)
        int headersize;
        @Element(sequence = 4)
        int fddsize;
        @Element(sequence = 5)
        int sectorsize;
        @Element(sequence = 6)
        int sectors;
        @Element(sequence = 7)
        int surfaces;
        @Element(sequence = 8)
        int cylinders;
    }
}

/* */
