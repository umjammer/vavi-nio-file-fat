/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import vavi.util.StringUtil;

import static org.junit.jupiter.api.Assertions.fail;



/**
 * RawIOTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/03 umjammer initial version <br>
 */
@EnabledOnOs(OS.WINDOWS)
public class RawIOTest {

    @Test
    @Disabled
    public void test() {
        fail("Not yet implemented");
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        exec2(args);
    }

    /** winRowIO */
    public static void exec2(String[] args) throws Exception {
        IOSource winRowIO = new WinRawIO("\\\\.\\" + args[0]);
        byte[] buffer = new byte[1024];
        winRowIO.readSector(buffer, 0);
        System.err.println(StringUtil.getDump(buffer));
    }

    /** winRowIO */
    public static void exec1(String[] args) throws Exception {
        for (int i = 0; i < 16; i++) {
            try {
                IOSource winRowIO = new WinRawIO("\\\\.\\PhysicalDrive" + i);
                byte[] buffer = new byte[1024];
                winRowIO.readSector(buffer, 0);
                System.err.println(StringUtil.getDump(buffer));
            } catch (IOException e) {
                System.err.println(e);
                System.err.println("no drive: " + i);
            }
        }
    }
}
