/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat32;

import java.nio.charset.Charset;
import java.util.Arrays;

import vavi.util.injection.Element;
import vavi.util.injection.Injector;


/**
 * Disk.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/09 umjammer initial version <br>
 */
public class Disk {

	public interface Sxsi {
		long getTotalSectors();
		long getCylinders();
		int getSectorSize();
		int getSectors();
		int getSurfaces();
		int getHeaderSize();
	}

	/** T98 HDD (IDE) */
	@Injector(bigEndian = false)
	public static class THDHeader implements Sxsi {
		@Element(sequence = 1, value = "unsigned int")
		int	cylinders;
		@Override
		public String toString() {
			return String.format(
					"THDHeader [cylinders=%s, getTotalSectors()=%s, getSectorSize()=%s, getSectors()=%s, getSurfaces()=%s, getHeaderSize()=%s]",
					cylinders, getTotalSectors(), getSectorSize(), getSectors(), getSurfaces(), getHeaderSize());
		}
		@Override
		public long getTotalSectors() {
			return cylinders * getSectorSize() * getSurfaces();
		}
		@Override
		public long getCylinders() {
			return cylinders;
		}
		@Override
		public int getSectorSize() {
			return 256;
		}
		@Override
		public int getSectors() {
			return 33;
		}
		@Override
		public int getSurfaces() {
			return 8;
		}
		@Override
		public int getHeaderSize() {
			return 256;
		}
	}

	// T98Next HDD (IDE)
	@Injector(bigEndian = false)
	public 	static class NHDHeader implements Sxsi {
		static final String signature = "T98HDDIMAGE.R0";
		@Element(sequence = 1)
		byte[] sig = new byte[16];
		@Element(sequence = 2, value = "0x100")
		String comment;
		@Element(sequence = 3, value = "unsigned int")
		long headersize;
		@Element(sequence = 4, value = "unsigned int")
		long cylinders;
		@Element(sequence = 5, value = "unsigned short")
		int surfaces;
		@Element(sequence = 6, value = "unsigned short")
		int sectors;
		@Element(sequence = 7, value = "unsigned short")
		int sectorsize;
		@Element(sequence = 8)
		byte[] reserved = new byte[0xe2];
		@Override
		public String toString() {
			return String.format(
					"NHDHeader [sig=%s, comment=%s, headersize=%s, cylinders=%s, surfaces=%s, sectors=%s, sectorsize=%s, reserved=%s]",
					Arrays.toString(sig), comment, headersize, cylinders, surfaces, sectors, sectorsize,
					Arrays.toString(reserved));
		}
		@Override
		public long getTotalSectors() {
			return cylinders * sectors * surfaces;
		}
		@Override
		public long getCylinders() {
			return cylinders;
		}
		@Override
		public int getSectorSize() {
			return sectorsize;
		}
		@Override
		public int getSectors() {
			return sectors;
		}
		@Override
		public int getSurfaces() {
			return surfaces;
		}
		@Override
		public int getHeaderSize() {
			return 0;
		}
	}

	/** ANEX86 HDD (SASI) thanx Mamiya */
	@Injector(bigEndian = false)
	public static class HDIHeader implements Sxsi {
		@Element(sequence = 1)
		byte[] dummy = new byte[4];
		@Element(sequence = 2)
		int hddtype;
		@Element(sequence = 3)
		int headersize;
		@Element(sequence = 4)
		int hddsize;
		@Element(sequence = 5)
		int sectorsize;
		@Element(sequence = 6)
		int sectors;
		@Element(sequence = 7)
		int surfaces;
		@Element(sequence = 8)
		int cylinders;
		@Override
		public String toString() {
			return String.format(
					"HDIHeader [dummy=%s, hddtype=%s, headersize=%s, hddsize=%s, sectorsize=%s, sectors=%s, surfaces=%s, cylinders=%s]",
					Arrays.toString(dummy), hddtype, headersize, hddsize, sectorsize, sectors, surfaces, cylinders);
		}
		@Override
		public long getTotalSectors() {
			return cylinders * sectors * surfaces;
		}
		@Override
		public long getCylinders() {
			return cylinders;
		}
		@Override
		public int getSectorSize() {
			return sectorsize;
		}
		@Override
		public int getSectors() {
			return sectors;
		}
		@Override
		public int getSurfaces() {
			return surfaces;
		}
		@Override
		public int getHeaderSize() {
			return headersize;
		}
	}

	/** Virtual98 HDD (SCSI) */
	@Injector(bigEndian = false)
	public static class VHDHeader implements Sxsi {
		static final String signature = "VHD1.00";
		@Element(sequence = 1)
		byte[] sig = new byte[3];
		@Element(sequence = 2, value = "unsigined byte")
		int ver;
		@Element(sequence = 3)
		byte delimita;
		@Element(sequence = 4, value = "128")
		String comment;
		@Element(sequence = 5)
		byte[] padding1 = new byte[4];
		@Element(sequence = 6, value = "unsigined short")
		int mbsize;
		@Element(sequence = 7, value = "unsigined short")
		int sectorsize;
		@Element(sequence = 8)
		int sectors;
		@Element(sequence = 9)
		int surfaces;
		@Element(sequence = 10, value = "unsigined short")
		int cylinders;
		@Element(sequence = 11)
		int totals;
		@Element(sequence = 12)
		byte[] padding2 = new byte[0x44];
		@Override
		public String toString() {
			return String.format(
					"VHDHeader [sig=%s, ver=%s, delimita=%s, comment=%s, padding1=%s, mbsize=%s, sectorsize=%s, sectors=%s, surfaces=%s, cylinders=%s, totals=%s, padding2=%s]",
					Arrays.toString(sig), ver, delimita, comment, Arrays.toString(padding1), mbsize, sectorsize, sectors,
					surfaces, cylinders, totals, Arrays.toString(padding2));
		}
		@Override
		public long getTotalSectors() {
			return totals;
		}
		@Override
		public long getCylinders() {
			return cylinders;
		}
		@Override
		public int getSectorSize() {
			return sectorsize;
		}
		@Override
		public int getSectors() {
			return sectors;
		}
		@Override
		public int getSurfaces() {
			return surfaces;
		}
		@Override
		public int getHeaderSize() {
			return 0;
		}
	}

	@Injector
	public static class BootRecord {
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
		@Element(sequence = 16, value = "unsigned short")
        int volumeSerialID;
		@Element(sequence = 17)
        byte[] volumeLabel = new byte[11];
		@Element(sequence = 18)
        byte[] fileSystem = new byte[8];
		@Override
		public String toString() {
			return String.format(
					"BootRecord [jump=%s, oemLavel=%s, bytesPerSector=%s, sectorsPerCluster=%s, reservedSectors=%s, numberOfFAT=%s, maxRootDirectoryEntries=%s, numberOfSmallSectors=%s, mediaDescriptor=%s, numberOfFATSector=%s, numberOfBIOSSector=%s, numberOfBIOSHeader=%s, invisibleSectors=%s, numberOfLargeSectors=%s, osData=%s, volumeSerialID=%s, volumeLabel=%s, fileSystem=%s]",
					Arrays.toString(jump), oemLavel, bytesPerSector, sectorsPerCluster, reservedSectors, numberOfFAT,
					maxRootDirectoryEntries, numberOfSmallSectors, mediaDescriptor, numberOfFATSector, numberOfBIOSSector,
					numberOfBIOSHeader, invisibleSectors, numberOfLargeSectors, Arrays.toString(osData), volumeSerialID,
					new String(volumeLabel), new String(fileSystem));
		}
    }

    @Injector
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
        /**開始シリンダ */
        @Element(sequence = 5, value = "unsigned short")
        int  startCylinder;
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
                            bootA,
                            bootB,
                            Arrays.toString(reservedA),
                            reservedB,
                            startCylinder,
                            reservedC,
                            endCylinder,
                            new String(name, Charset.forName("shift_jis")));
        }
    }

    @Injector
    public static class masterBootRecordAT {
        /** プログラム */
        @Element(sequence = 1)
        byte[] program = new byte[446];
        /** 領域情報 */
        @Element(sequence = 2)
        PartEntryAT[] parts = new PartEntryAT[4];
        /** 予約 */
        @Element(sequence = 3)
        byte[] reserved2 = new byte[2];
    }

    @Injector
    public static class PartEntryAT {
        /** 領域状態(0x00:スリープ、0x80:アクティブ) */
        @Element(sequence = 1, value = "unsigned byte")
        int status;
        /** 領域の開始ヘッド */
        @Element(sequence = 2, value = "unsigned byte")
        int head;
        /** 領域の開始シリンダ・セクタ */
        @Element(sequence = 3, value = "unsigned short")
        int cylsec;
        /** 領域の種類 */
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
        /** 領域のセクタ数e */
        @Element(sequence = 8)
        int nsecs;
    }

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
