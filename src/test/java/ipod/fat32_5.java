/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ipod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import vavix.io.WinRawIO;
import vavix.io.fat.FileAllocationTable;
import vavix.util.ByteArrayMatcher;
import vavix.util.Matcher;


/**
 * fat32 forensic 5.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_5 {

    /**
     * search word in cluster
     * @param args 0:device, 1:cluster list, 2:word
     */
    public static void main(String[] args) throws Exception {
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        String file = args[1];
        String word = args[2];
System.err.println("word: " + word);

        int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();
        byte[] buffer = new byte[bytesPerCluster]; 
        boolean found = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file))));
        while (reader.ready()) {
            String line = reader.readLine();
            int cluster = Integer.parseInt(line);
//System.err.println("cluster: " + cluster);
            fat32.readCluster(buffer, cluster);
            Matcher<byte[]> matcher = new ByteArrayMatcher(buffer);
            int index = matcher.indexOf(word.getBytes(System.getProperty("file.encoding")), 0);
            if (index != -1) {
System.err.println("\nfound: " + word + " at " + cluster + ", index " + index);
                found = true;
            } else {
System.err.print(".");
System.err.flush();
            }
        }
        reader.close();
System.err.println();
        if (!found) {
System.err.println("not found: " + word);
        }
    }
}
