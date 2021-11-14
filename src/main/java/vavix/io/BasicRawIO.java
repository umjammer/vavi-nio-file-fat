/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import vavi.io.LittleEndianSeekableDataInputStream;
import vavi.io.SeekableDataInput;


/**
 * BasicRawIO.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/06 umjammer initial version <br>
 */
public class BasicRawIO implements IOSource {

    /** 1 セクタのバイト数 */
    private int bytesPerSector;
    private int offset;

    protected SeekableDataInput sdi;

	/** */
    public BasicRawIO(String deviceName, int bytesPerSector, int offset) throws IOException {
        this.bytesPerSector = bytesPerSector;
        this.offset = 512;
    	sdi = new LittleEndianSeekableDataInputStream(Files.newByteChannel(Paths.get(deviceName)));
        sdi.position(offset);
    }

    /* @see vavix.io.IRawIO#readSector(byte[], int) */
    @Override
	public int readSector(byte[] buffer, int sectorNo) throws IOException {
        sdi.position(offset + sectorNo * bytesPerSector);
        sdi.readFully(buffer, 0, bytesPerSector);
        return bytesPerSector;
    }

    @Override
    public int getBytesPerSector() {
    	return bytesPerSector;
    }
}

/* */
