/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;


public interface IOSource {

	/** */
	int readSector(byte[] buffer, int sectorNo) throws IOException;

	/** */
	int getBytesPerSector();
}

/* */
