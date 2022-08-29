/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.partition;

import java.util.Arrays;

import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * PC98PartitionEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 * @see "https://github.com/aaru-dps/Aaru/blob/devel/Aaru.Partitions/PC98.cs"
 * @see "https://github.com/jilinxpd/illumos-smbfs-mmap/blob/fa064073ffa71dc6c7e0ae9b2f055e77149f1693/usr/src/lib/libparted/common/libparted/labels/pc98.c#L55"
 */
@Serdes(bigEndian = false)
public class PC98PartitionEntry {

    /**
     * <pre>
     * bit 7: 1=bootable, 0=not bootable
     *  # Linux uses this flag to make a distinction between ext2 and swap.
     * bit 6--0:
     *  00H      : N88-BASIC(data)?, PC-UX(data)?
     *  04H      : PC-UX(data)
     *  06H      : N88-BASIC
     *  10H      : N88-BASIC
     *  14H      : *BSD, PC-UX
     *  20H      : DOS(data), Windows95/98/NT, Linux
     *  21H..2FH : DOS(system#1 .. system#15)
     *  40H      : Minix
     * </pre>
     */
    @Element(sequence = 1, value = "unsigned byte")
    int mid;

    /**
     * <pre>
     * bit 7: 1=active, 0=sleep(hidden)
     *  # PC-UX uses this flag to make a distinction between its file system
     *  # and its swap.
     * bit 6--0:
     *  01H: FAT12
     *  11H: FAT16, <32MB [accessible to DOS 3.3]
     *  21H: FAT16, >=32MB [Large Partition]
     *  31H: NTFS
     *  28H: Windows NT (Volume/Stripe Set?)
     *  41H: Windows NT (Volume/Stripe Set?)
     *  48H: Windows NT (Volume/Stripe Set?)
     *  61H: FAT32
     *  04H: PC-UX
     *  06H: N88-BASIC
     *  44H: *BSD
     *  62H: ext2, linux-swap
     * </pre>
     */
    @Element(sequence = 2, value = "unsigned byte")
    int sid;

    /** */
    @Element(sequence = 3, value = "unsigned byte")
    int dum1;
    /** */
    @Element(sequence = 4, value = "unsigned byte")
    int dum2;

    /** */
    @Element(sequence = 5, value = "unsigned byte")
    int iplSector;
    /** */
    @Element(sequence = 6, value = "unsigned byte")
    int iplHeader;
    /** */
    @Element(sequence = 7, value = "unsigned short")
    int iplCylinder;

    /** */
    @Element(sequence = 8, value = "unsigned byte")
    public int startSector;
    /** */
    @Element(sequence = 9, value = "unsigned byte")
    public int startHeader;
    /** 開始シリンダ */
    @Element(sequence = 10, value = "unsigned short")
    public int startCylinder;

    /** */
    @Element(sequence = 11, value = "unsigned byte")
    public int endSector;
    /** */
    @Element(sequence = 12, value = "unsigned byte")
    public int endHeader;
    /** 終了シリンダ */
    @Element(sequence = 13, value = "unsigned short")
    public int endCylinder;

    /** 領域名(16文字に満たない場合の残りの部分は0x20) */
    @Element(sequence = 14)
    byte[] name = new byte[16];

    @Override
    public String toString() {
        return String
                .format("PC98PartitionEntry [mid=%s, sid=%s, dum1=%s, dum2=%s, iplSector=%s, iplHeader=%s, iplCylinder=%s, startSector=%s, startHeader=%s, startCylinder=%s, endSector=%s, endHeader=%s, endCylinder=%s, name=%s]",
                        mid, sid, dum1, dum2, iplSector, iplHeader, iplCylinder, startSector, startHeader, startCylinder, endSector, endHeader, endCylinder,
                        new String(name));
    }

    // TODO
    public boolean isValid() {
        if ((mid & 0xf0) == 0 || (sid & 0xf0) == 0) {
            return false;
        }
        if (!new String(name).startsWith("MS-DOS")) {
            return false;
        }
        return true;
    }
}

/* */
