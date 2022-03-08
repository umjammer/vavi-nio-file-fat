/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import vavi.util.Debug;


/**
 * BasicRawIO.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/06 umjammer initial version <br>
 */
public class BasicRawIO implements IOSource {

    /** */
    private int bytesPerSector = 512;
    private long offset;

    private SeekableByteChannel sbc;

    /** */
    public BasicRawIO(String deviceName) throws IOException {
        sbc = Files.newByteChannel(Paths.get(deviceName));
    }

    /** */
    public BasicRawIO(String deviceName, int bytesPerSector, int offset) throws IOException {
        this.bytesPerSector = bytesPerSector;
        this.offset = offset;
        sbc = Files.newByteChannel(Paths.get(deviceName));
        sbc.position(offset);
    }

    @Override
    public int readSector(byte[] buffer, int sectorNo) throws IOException {
Debug.printf("readSector: %d, %08x\n", sectorNo, sectorNo * bytesPerSector);
        sbc.position(offset + sectorNo * bytesPerSector);
        sbc.read(ByteBuffer.wrap(buffer));
        return bytesPerSector;
    }

    @Override
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    @Override
    public void setOffset(long offset) {
        this.offset = offset;
    }
}

/* */
