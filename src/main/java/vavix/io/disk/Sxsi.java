/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.disk;


/**
 * Sxsi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/12 umjammer initial version <br>
 */
public interface Sxsi {

    long getTotalSectors();
    long getCylinders();
    int getSectorSize();
    int getSectors();
    int getSurfaces();
    int getHeaderSize();

    /*
    FileChannel fc;
    Sxsi sxsi;

    void read(long pos, byte[] buf, int size) throws IOException {

        long r;
        int rsize;

        if ((pos < 0) || (pos >= sxsi.getTotalSectors())) {
            throw new IllegalArgumentException("pos");
        }
        pos = pos * sxsi.getSectorSize() + sxsi.getHeaderSize();
        fc.position(pos);
        while (size > 0) {
            rsize = Math.min(size, sxsi.getSectorSize());
            fc.position(pos);
            if (file_read((FILEH) sxsi.fh, buf, rsize) != rsize) {
                return (0xd0);
            }
            buf += rsize;
            size -= rsize;
        }
    }

    void write(long pos, final byte[] buf, int size) throws IOException {

        long r;
        int wsize;

        if ((pos < 0) || (pos >= sxsi.getTotalSectors())) {
            throw new IllegalArgumentException("pos");
        }
        pos = pos * sxsi.size + sxsi.headersize;
        r = file_seek((FILEH) sxsi.fh, pos, FSEEK_SET);
        if (pos != r) {
            return (0xd0);
        }
        while (size) {
            wsize = min(size, sxsi.size);
            CPU_REMCLOCK -= wsize;
            if (file_write((FILEH) sxsi.fh, buf, wsize) != wsize) {
                return (0x70);
            }
            buf += wsize;
            size -= wsize;
        }
    }

    void format(long pos) throws IOException {

        long r;
        int i;
        byte[]work = new byte[256];
        int size;
        int wsize;

        if ((pos < 0) || (pos >= sxsi.getTotalSectors())) {
            throw new IllegalArgumentException("pos");
        }
        pos = pos * sxsi.size + sxsi.getHeaderSize();
        r = file_seek((FILEH) sxsi.fh, pos, 0);
        if (pos != r) {
            return (0xd0);
        }
        Arrays.fill(work, 0, work.length, (byte) 0xe5);
        for (i = 0; i < sxsi.getSectors(); i++) {
            size = sxsi.size;
            while (size) {
                wsize = Math.min(size, work.length);
                size -= wsize;
                if (file_write((FILEH) sxsi.fh, work, wsize) != wsize) {
                    return (0x70);
                }
            }
        }
    }
    */
}

/* */
