/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.partition;

import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * ATPartitionEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 * @see "http://hp.vector.co.jp/authors/VA013937/editdisk/tech.html"
 */
@Serdes(bigEndian = false)
public class ATPartitionEntry {
    /** 0x00: non bootable, 0x80: bootable */
    @Element(sequence = 1, value = "unsigned byte")
    int status;
    /** 領域の開始ヘッド */
    @Element(sequence = 2, value = "unsigned byte")
    int startHeader;
    /** 領域の開始シリンダ・セクタ */
    @Element(sequence = 3, value = "unsigned short")
    int startCylSec;
    /**
     * 領域の種類
     * <ul>
     * <li> 0x00:不明
     * <li> 0x01:FAT12
     * <li> 0x04:FAT16(32MB以下)
     * <li> 0x05:拡張MSDOS領域
     * <li> 0x06:FAT16(32MBより大きい)
     * <li> 0x0B:FAT32
     * <li> 0x0C:FAT32(LBA 拡張int13h)
     * <li> 0x0E:FAT16(LBA 拡張int13h)
     * <li> 0x0F:拡張MSDOS領域(LBA 拡張int13h)
     * </ul>
     */
    @Element(sequence = 4, value = "unsigned byte")
    int type;
    /** 領域の終了ヘッド */
    @Element(sequence = 5, value = "unsigned byte")
    int endHeader;
    /** 領域の終了シリンダ・セクタ */
    @Element(sequence = 6, value = "unsigned short")
    int endCylSec;
    /** MBRの先頭から領域の先頭までのセクタ数 */
    @Element(sequence = 7)
    int startSector;
    /** 領域のセクタ数 */
    @Element(sequence = 8)
    int numberOfSectors;
    public boolean isBootable() {
        return (status & 0x80) != 0;
    }
    int startSec() {
        return _sec(startCylSec); 
    }
    int endSec() {
        return _sec(endCylSec); 
    }
    int startCyl() {
        return _cyl(startCylSec); 
    }
    int endCyl() {
        return _cyl(endCylSec); 
    }
    private static int _cyl(int x) {
        return (x >> 8) | (x & 0xc0) << 2;
    }
    private static int _sec(int x) {
        return x & 0x3f;
    }
    @Override
    public String toString() {
        return String
                .format("ATPartitionEntry [status=%s, startHeader=%s, startCylSec=%s, type=%s, endHeader=%s, endCylSec=%s, startSector=%s, numberOfSectors=%s]",
                        status, startHeader, startCylSec, type, endHeader, endCylSec, startSector, numberOfSectors);
    }
}

/* */
