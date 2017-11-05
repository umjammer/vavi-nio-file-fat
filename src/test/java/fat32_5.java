/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import vavix.io.fat32.Fat32;


/**
 * fat32_5.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_5 {
    /**
     * serach word in cluster 
     * @param args 0:device, 1:cluster list, 2:word
     */
    public static void main(String[] args) throws Exception {
        Fat32 fat32 = new Fat32(args[0]);
        String file = args[1];
        String word = args[2];
System.err.println("word: " + word);

        byte[] buffer = new byte[fat32.getBytesPerCluster()]; 
        boolean found = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        while (reader.ready()) {
            String line = reader.readLine();
            int cluster = Integer.parseInt(line);
//System.err.println("cluster: " + cluster);
            fat32.readCluster(buffer, cluster);
            int index = fat32.matcher(buffer).indexOf(word.getBytes(System.getProperty("file.encoding")), 0);
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

/* */
