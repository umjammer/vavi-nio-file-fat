/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.disk;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vavi.io.ChannelInputStream;
import vavi.util.injection.Injector;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import vavix.io.disk.FDI;
import vavix.io.fat.Disk;
import vavix.io.fat.Disk.BootRecord;


@PropsEntity(url = "file://${user.dir}/local.properties")
class FDITest {

    @Property(name = "test.fdi")
    String file;

    @BeforeEach
    void setup() throws IOException {
        PropsEntity.Util.bind(this);
    }

    @Test
    void test() throws Exception {
        FileInputStream fis = new FileInputStream(file);
        FileChannel fc = fis.getChannel();

        FDI.Header header = new FDI.Header();
        Injector.Util.inject(new ChannelInputStream(fc), header);

        fc.position(header.headersize);

        Disk.BootRecord br = new Disk.BootRecord();
        Injector.Util.inject(new ChannelInputStream(fc), br);

        fis.close();
    }
}

/* */
