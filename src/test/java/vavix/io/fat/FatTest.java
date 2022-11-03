/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import vavi.util.serdes.Serdes;
import vavix.io.BasicRawIO;
import vavix.io.IOSource;
import vavix.io.partition.ATMasterBootRecord;
import vavix.io.partition.ATPartitionEntry;


/**
 * FatTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/07 umjammer initial version <br>
 */
@PropsEntity(url = "file://${user.dir}/local.properties")
public class FatTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Property(name = "test.fat16")
    String fat16 = "src/test/resources/fat16.dmg";

    @Property(name = "test.fat32")
    String fat32;

    @Test
    @DisplayName("AT fat16, parted(dmg)")
    public void test() throws IOException {

        // partition
        IOSource ios = new BasicRawIO(fat16);
        byte[] bs = new byte[512];
        ios.readSector(bs, 0);

        ATMasterBootRecord mbr = new ATMasterBootRecord();
        Serdes.Util.deserialize(new ByteArrayInputStream(bs), mbr);
Debug.println(mbr);
        for (int i = 0; i < 4; i++) {
            ATPartitionEntry pe = mbr.partitionEntries[i];
if (pe.isBootable()) {
 System.err.println(pe);
}
        }

        // set partition start is 0, skip mbr size
        ios.setOffset(512);

        // each partition
        FileAllocationTable fat = new FileAllocationTable(ios);
Debug.println(fat16);
Debug.println(fat.bpb);
Debug.println(fat.fat);
Debug.println("fat: " + fat.fat);
        list(fat, "\\");
    }

    @Test
    @EnabledIf("localPropertiesExists")
    @DisplayName("AT fat32, raw partition")
    public void test2() throws IOException {
        FileAllocationTable fat = new FileAllocationTable(new BasicRawIO(fat32, 512, 0));
Debug.println(fat32);
Debug.println(fat.bpb);
Debug.println(fat.fat);
        list(fat, "\\");
    }

    /** */
    void list(FileAllocationTable fat, String path) throws IOException {
        DirectoryEntry directory = fat.getDirectoryEntry(path);
        for (FileEntry entry : directory.entries().stream().filter(e -> !(e instanceof DeletedEntry)).collect(Collectors.toList())) {
            if (!entry.isDirectory()) {
System.err.println(path + entry + ", start: " + entry.getStartCluster() + (Debug.isLoggable(Level.INFO) ? "\n" + StringUtil.getDump(fat.getInputStream(entry), 0, 64) : ""));
            } else {
                if (!entry.getName().equals(".") && !entry.getName().equals("..")) {
System.err.println(path + entry + ", start: " + entry.getStartCluster());
                    list(fat, path + entry.getName() + "\\");
                }
            }
        }
    }

    //----

    /**
     * @param args 0:dir F:\xxx\yyy
     */
    public static void main(String[] args) throws Exception {
        FatTest app = new FatTest();
        PropsEntity.Util.bind(app);
        app.t1(args);
    }

    void t1(String[] args) throws Exception {
        FileAllocationTable fat = new FileAllocationTable(new BasicRawIO(fat32, 512, 512));
        DirectoryEntry directory = fat.getDirectoryEntry("\\");
for (FileEntry entry : directory.entries()) {
 System.err.println(entry.getName() + ": " + entry.getStartCluster());
}
    }
}

/* */
