/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat32;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Test;

import vavi.io.LittleEndianSeekableDataInputStream;
import vavi.util.injection.Injector;

import vavix.io.BasicRawIO;
import vavix.io.fat32.Fat32.FileEntry;


/**
 * Fat32Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/07 umjammer initial version <br>
 */
public class Fat32Test {

	@Test
	public void test() throws IOException {
        String file = "/Volumes/GoogleDrive/My Drive/Private/Computer/Games/tmp/nsano.nhd";
        Fat32 fat32 = new Fat32(new BasicRawIO(file, 512, 512));
System.err.println(fat32.bpb);
System.err.println(fat32.fat);
        Map<String, FileEntry> entries = fat32.getEntries("\\");
for (FileEntry entry : entries.values()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
	}

    //----

    /**
     * @param args 0:dir F:\xxx\yyy
     */
    public static void main(String[] args) throws Exception {
        t2(args);
    }

    public static void t1(String[] args) throws Exception {
        String file = "/Volumes/GoogleDrive/My Drive/Private/Computer/Games/tmp/nsano.nhd";
        Fat32 fat32 = new Fat32(new BasicRawIO(file, 512, 512));
        Map<String, FileEntry> entries = fat32.getEntries("\\");
for (FileEntry entry : entries.values()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
    }

    public static void t2(String[] args) throws Exception {
        Path path = Paths.get("/Volumes/GoogleDrive/My Drive/Private/Computer/Games/tmp/nsano.nhd");
    	LittleEndianSeekableDataInputStream lesis = new LittleEndianSeekableDataInputStream(Files.newByteChannel(path));
    	Disk.NHDHeader header = new Disk.NHDHeader();
    	Injector.Util.inject(lesis, header);
System.err.println("■ header ----\n" + header);

        lesis.position(0x200);
    	Disk.BootRecord bootRecord = new Disk.BootRecord();
    	Injector.Util.inject(lesis, bootRecord);
System.err.println("■ bootRecord ----\n" + bootRecord);

        lesis.position(0x400);
        for (int i = 0; i < 8; i++) {
            Disk.PartEntry98 partEntry98 = new Disk.PartEntry98();
            Injector.Util.inject(lesis, partEntry98);
            if (partEntry98.bootA != 0) {
System.err.println("■ partEntry98[" + i + "] ----\n" + partEntry98);
            }
        }
    }
}

/* */
