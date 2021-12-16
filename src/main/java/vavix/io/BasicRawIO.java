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
import vavi.util.injection.Injector;

import vavix.io.fat.Disk;


/**
 * BasicRawIO.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/06 umjammer initial version <br>
 */
public class BasicRawIO implements IOSource {

    /** */
    private int bytesPerSector = 512;
    private int offset;

    private SeekableByteChannel sbc;

    /** */
    public BasicRawIO(String deviceName) throws IOException {
        sbc = Files.newByteChannel(Paths.get(deviceName));

        Disk.MasterBootRecordAT mbr = new Disk.MasterBootRecordAT();
        Injector.Util.inject(sbc, mbr);
System.err.println(mbr);
        Disk.PartEntryAT pe = new Disk.PartEntryAT();
        for (int i = 0; i < 4; i++) {
            Injector.Util.inject(sbc, pe);
if (pe.isBootable()) {
 System.err.println(pe);
}
        }
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
        ByteBuffer bb = ByteBuffer.allocate(bytesPerSector);
        sbc.read(bb);
        System.arraycopy(bb.array(), 0, buffer, 0, bytesPerSector);
        return bytesPerSector;
    }

    @Override
    public int getBytesPerSector() {
        return bytesPerSector;
    }

    /** */
    public void setBytesPerSector(int bytesPerSector) {
        this.bytesPerSector = bytesPerSector;
    }
}

/* */
