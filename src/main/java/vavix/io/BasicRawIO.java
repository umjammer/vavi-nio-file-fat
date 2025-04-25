/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.getLogger;


/**
 * Deals a solid disk image that has {@link #offset} bytes like a disk header,
 * and after that there are sectors that has {@link #bytesPerSector} bytes.
 * <pre>
 *  +--------------+
 *  | header       | {@link #offset} bytes
 *  +--------------+
 *  | sector 0     | {@link #bytesPerSector} bytes
 *  +--------------+
 *  | sector 1     | {@link #bytesPerSector} bytes
 *  +--------------+
 *  | sector 2     |               ⋮
 *  +--------------+
 *  | sector 3     |
 *         ⋮
 * </pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/06 umjammer initial version <br>
 */
public class BasicRawIO implements IOSource {

    private static final Logger logger = getLogger(BasicRawIO.class.getName());

    /** */
    private int bytesPerSector = 512;
    private long offset;

    private final SeekableByteChannel sbc;

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
logger.log(Level.DEBUG, "readSector: %d, %08x".formatted(sectorNo, sectorNo * bytesPerSector));
        sbc.position(offset + (long) sectorNo * bytesPerSector);
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
