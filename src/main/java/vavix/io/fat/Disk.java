/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.nio.charset.Charset;
import java.util.Arrays;

import vavi.util.Debug;
import vavi.util.injection.Element;
import vavi.util.injection.Injector;

import vavix.io.fat.FileAllocationTable.BiosParameterBlock;
import vavix.io.fat.FileAllocationTable.FatType;


/**
 * Disk.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/09 umjammer initial version <br>
 * @see http://hp.vector.co.jp/authors/VA013937/editdisk/tech.html
 */
public class Disk {

    /** for pc98 */
    @Injector(bigEndian = false)
    public static class BootRecord implements BiosParameterBlock {
        @Element(sequence = 1)
        byte[] jump = new byte[3];
        @Element(sequence = 2, value = "8")
        String oemLavel;
        @Element(sequence = 3, value = "unsigned short")
        int bytesPerSector;
        @Element(sequence = 4, value = "unsigned byte")
        int sectorsPerCluster;
        @Element(sequence = 5, value = "unsigned short")
        int reservedSectors;
        @Element(sequence = 6, value = "unsigned byte")
        int numberOfFAT;
        @Element(sequence = 7, value = "unsigned short")
        int maxRootDirectoryEntries;
        @Element(sequence = 8, value = "unsigned short")
        int numberOfSmallSectors;
        @Element(sequence = 9, value = "unsigned byte")
        int mediaDescriptor;
        @Element(sequence = 10, value = "unsigned short")
        int numberOfFATSector;
        @Element(sequence = 11, value = "unsigned short")
        int numberOfBIOSSector;
        @Element(sequence = 12, value = "unsigned short")
        int numberOfBIOSHeader;
        @Element(sequence = 13)
        int invisibleSectors;
        @Element(sequence = 14)
        int numberOfLargeSectors;
        @Element(sequence = 15)
        byte[] osData = new byte[3];
        @Element(sequence = 16)
        int volumeSerialID;
        @Element(sequence = 17, value = "11")
        String volumeLabel;
        @Element(sequence = 18, value = "8")
        String fileSystem;
        @Override
        public String toString() {
            return String.format(
                    "BootRecord [jump=%s, oemLavel=%s, bytesPerSector=%s, sectorsPerCluster=%s, reservedSectors=%s, numberOfFAT=%s, maxRootDirectoryEntries=%s, numberOfSmallSectors=%s, mediaDescriptor=%s, numberOfFATSector=%s, numberOfBIOSSector=%s, numberOfBIOSHeader=%s, invisibleSectors=%s, numberOfLargeSectors=%s, osData=%s, volumeSerialID=%s, volumeLabel=%s, fileSystem=%s]",
                    Arrays.toString(jump), oemLavel, bytesPerSector, sectorsPerCluster, reservedSectors, numberOfFAT,
                    maxRootDirectoryEntries, numberOfSmallSectors, mediaDescriptor, numberOfFATSector, numberOfBIOSSector,
                    numberOfBIOSHeader, invisibleSectors, numberOfLargeSectors, Arrays.toString(osData), volumeSerialID,
                    new String(volumeLabel), new String(fileSystem));
        }
        // TODO
        public boolean validate() {
            if (!oemLavel.startsWith("NEC")) {
                return false;
            }
            return true;
        }
        @Override
        public int getSectorsPerCluster() {
            return sectorsPerCluster;
        }
        @Override
        public int getStartClusterOfRootDirectory() {
            return reservedSectors + numberOfFAT * numberOfFATSector;
        }
        @Override
        public int getBytesPerSector() {
            return bytesPerSector;
        }
        @Override
        public int getFatSector(int fatNumber) {
//Debug.printf("reservedSectors: %d, fatNumber: %d, numberOfFATSector: %d, result: %d%n", reservedSectors, fatNumber, numberOfFATSector, reservedSectors + fatNumber * numberOfFATSector);
            return reservedSectors + fatNumber * numberOfFATSector;
        }
        @Override
        public int getLastCluster() {
            return (int) ((numberOfLargeSectors + (sectorsPerCluster - 1)) / sectorsPerCluster);
        }
        @Override
        public int toSector(int cluster) {
            int sector = cluster * sectorsPerCluster;
            return sector;
        }
        @Override
        public int getFatSectors() {
            return numberOfFATSector;
        }
        @Override
        public FatType getFatType() {
Debug.println("filesystem: [" + fileSystem.replaceFirst("\\s*$", "") + "]");
            switch (fileSystem.replaceFirst("\\s*$", "")) {
            default: // TODO
            case "FAT12":
                return FatType.Fat12Fat;
            case "FAT16":
                return FatType.Fat16Fat;
            case "FAT32":
                return FatType.Fat32Fat;
            }
        }
    }

    @Injector(bigEndian = false)
    public static class PartEntry98 {
        /** ブート種別(詳細不明) */
        @Element(sequence = 1, value = "unsigned byte")
        int bootA;
        /** ブート種別(詳細不明、上記と共にアクティブ、スリープ、起動可不可、容量などで変化) */
        @Element(sequence = 2, value = "unsigned byte")
        int bootB;
        /** 不明(0フィルでもない?) */
        @Element(sequence = 3)
        byte[] reservedA= new byte[6];
        /** 不明 */
        @Element(sequence = 4, value = "unsigned short")
        int reservedB;
        /** 開始シリンダ */
        @Element(sequence = 5, value = "unsigned short")
        public int startCylinder;
        /** 不明 */
        @Element(sequence = 6, value = "unsigned short")
        int reservedC;
        /** 終了シリンダ */
        @Element(sequence = 7, value = "unsigned short")
        int endCylinder;
        /** 領域名(16文字に満たない場合の残りの部分は0x20) */
        @Element(sequence = 8)
        byte[] name = new byte[16];
        @Override
        public String toString() {
            return String
                    .format("PartEntry98 [bootA=%s, bootB=%s, reservedA=%s, reservedB=%s, startCylinder=%s, reservedC=%s, endCylinder=%s, name=%s]",
                            bootA, bootB, Arrays.toString(reservedA), reservedB, startCylinder, reservedC, endCylinder, new String(name, Charset.forName("shift_jis")));
        }
        // TODO
        public boolean validate() {
            if (bootA != 0xa1 || bootB != 0xa1) {
                return false;
            }
            if (!new String(name).startsWith("MS-DOS")) {
                return false;
            }
            return true;
        }
    }

    @Injector(bigEndian = false)
    public static class MasterBootRecordAT {
        /** プログラム */
        @Element(sequence = 1)
        byte[] bootStrapCode = new byte[446];
        /** 領域情報 */
        @Element(sequence = 2)
        PartEntryAT[] partitionEntries = new PartEntryAT[4];
        /** 0xaa55 le */
        @Element(sequence = 3)
        byte[] bootSignature = new byte[2];
    }

    @Injector(bigEndian = false)
    public static class PartEntryAT {
        /** 0x00: non bootable, 0x80: bootable */
        @Element(sequence = 1, value = "unsigned byte")
        int status;
        /** 領域の開始ヘッド */
        @Element(sequence = 2, value = "unsigned byte")
        int head;
        /** 領域の開始シリンダ・セクタ */
        @Element(sequence = 3, value = "unsigned short")
        int cylsec;
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
        int headend;
        /** 領域の終了シリンダ・セクタ */
        @Element(sequence = 6, value = "unsigned short")
        int cylsecend;
        /** MBRの先頭から領域の先頭までのセクタ数 */
        @Element(sequence = 7)
        int sec;
        /** 領域のセクタ数 */
        @Element(sequence = 8)
        int nsecs;
        public boolean isBootable() {
            return (status & 0x80) != 0;
        }
        int startSec() {
            return _sec(cylsec); 
        }
        int endSec() {
            return _sec(cylsecend); 
        }
        int startCyl() {
            return _cyl(cylsec); 
        }
        int endCyl() {
            return _cyl(cylsecend); 
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
                    .format("PartEntryAT [status=0x%02x, head=%s, type=%s, headend=%s, sec=%s, nsecs=%s, isActive()=%s, startSec()=%s, endSec()=%s, startCyl()=%s, endCyl()=%s]",
                            status, head, type, headend, sec, nsecs, isBootable(), startSec(), endSec(), startCyl(), endCyl());
        }
    }
}
