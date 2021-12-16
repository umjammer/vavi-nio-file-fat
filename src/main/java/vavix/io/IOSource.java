/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;


/**
 * Reader using the sector as an unit.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/03 umjammer initial version <br>
 */
public interface IOSource {

    /**
     * @return bytes read
     */
    int readSector(byte[] buffer, int sectorNo) throws IOException;

    /**
     * Returns bytes per a cluster.
     */
    int getBytesPerSector();
}

/* */
