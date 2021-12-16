/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import vavi.io.LittleEndianSeekableDataInputStream;
import vavi.util.Debug;
import vavi.util.injection.Injector;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import vavix.io.BasicRawIO;
import vavix.io.IOSource;
import vavix.io.disk.NHDHeader;
import vavix.io.fat.FileAllocationTable.FileEntry;


/**
 * FatTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/07 umjammer initial version <br>
 */
//@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file://${user.dir}/local.properties")
public class FatTest {

//    static boolean localPropertiesExists() {
//        return Files.exists(Paths.get("local.properties"));
//    }

//    @BeforeEach
//    void setup() throws IOException {
//        PropsEntity.Util.bind(this);
//    }

    // TODO doesn't work
    @Test
    public void test() throws IOException {
        FileAllocationTable fat32 = new FileAllocationTable(new BasicRawIO("src/test/resources/fat32.dmg"));
System.err.println(fat32.bpb);
System.err.println(fat32.fat);
Debug.println("fat: " + fat32.fat);
        Map<String, FileEntry> entries = fat32.getEntries("\\");
for (FileEntry entry : entries.values()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
    }

    // TODO doesn't work
    @Test
    @Disabled
    public void test2() throws IOException {
        FileAllocationTable fat32 = new FileAllocationTable(new BasicRawIO("src/test/resources/fat32.dd"));
System.err.println(fat32.bpb);
System.err.println(fat32.fat);
        Map<String, FileEntry> entries = fat32.getEntries("\\");
for (FileEntry entry : entries.values()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
    }

    //----

    @Property(name = "test.fat32")
    String file;

    /**
     * @param args 0:dir F:\xxx\yyy
     */
    public static void main(String[] args) throws Exception {
        FatTest app = new FatTest();
        PropsEntity.Util.bind(app);
        app.t2(args);
    }

    void t1(String[] args) throws Exception {
        FileAllocationTable fat32 = new FileAllocationTable(new BasicRawIO(file, 512, 512));
        Map<String, FileEntry> entries = fat32.getEntries("\\");
for (FileEntry entry : entries.values()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
    }

    /** nhd: ok */
    void t2(String[] args) throws Exception {
        Path path = Paths.get(file);
        LittleEndianSeekableDataInputStream lesis = new LittleEndianSeekableDataInputStream(Files.newByteChannel(path));

        // 1. header

        NHDHeader header = new NHDHeader();
        Injector.Util.inject(lesis, header);
System.err.println("■ header ----\n" + header);

        // 2. partition entries

        lesis.position(header.getHeaderSize() + header.getSectorSize());  // sector 2
        int i = 0;
//        for (int i = 0; i < 8; i++) {
            Disk.PartEntry98 partEntry98 = new Disk.PartEntry98();
            Injector.Util.inject(lesis, partEntry98);
            if (partEntry98.validate()) {
System.err.println("■ partEntry98[" + i + "] ----\n" + partEntry98);
            }
//        }

        // 3. mbr

        int offset = header.getHeaderSize() + header.getSectorSize() * (header.getSectors() * header.getSurfaces()) * partEntry98.startCylinder;
        lesis.position(offset);
        Disk.BootRecord bpb = new Disk.BootRecord();
        Injector.Util.inject(lesis, bpb);
System.err.println("■ bootRecord ----\n" + bpb);

        // 4. fat

        IOSource io = new IOSource() {
            @Override
            public int readSector(byte[] buffer, int sectorNo) throws IOException {
                int pos = offset + getBytesPerSector() * (sectorNo - 2);
                lesis.position(pos);
Debug.printf("pos: 0x%08X\n", pos);
                lesis.read(buffer, 0, getBytesPerSector());
                return getBytesPerSector();
            }
            @Override
            public int getBytesPerSector() {
                return bpb.getBytesPerSector();
            }
        };

        FileAllocationTable fat = new FileAllocationTable(io, bpb);
        fat.getEntries("\\").values().forEach(System.err::println);
    }
}

/* */
