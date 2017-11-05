/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import vavi.util.StringUtil;
import vavix.io.fat32.Fat32;
import vavix.io.fat32.Fat32.Fat;


/**
 * fat32_4.
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2006/01/09 nsano initial version <br>
 */
public class fat32_4 {

    /** */
    public static void main(String[] args) throws Exception {
        new fat32_4(args);
    }

    //-------------------------------------------------------------------------

    /** */
    private fat32_4(String[] args) throws Exception {
        exec5(args);
    }

    /** */
    Fat32 fat32;

    /** */
    void setUserCluster() throws Exception {
        Fat fat = fat32.useUserFat();
        //
        Scanner scanner = new Scanner(new FileInputStream("uc1.uc"));
        while (scanner.hasNextInt()) {
            // TSV
            int startCluster = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("startCluster: " + startCluster + ", size: " + size);
            List<Integer> clusters = new ArrayList<>(); 
            for (int i = 0; i < fat32.getRequiredClusters(size); i++) {
                clusters.add(startCluster + i);
            }
            fat.setClusterChain(clusters.toArray(new Integer[clusters.size()]));
        } 
        scanner.close();
        //
        scanner = new Scanner(new FileInputStream("uc2.uc"));
        while (scanner.hasNextInt()) {
            // TSV
            int size = scanner.nextInt();
            int startCluster = scanner.nextInt();
            int lastCluster = scanner.nextInt();
            int size2nd = scanner.nextInt();
System.err.println("startCluster: " + startCluster + ", size: " + size + ", lastCluster: "+ lastCluster + ", size2nd: " + size2nd);
            List<Integer> clusters = new ArrayList<>(); 
            int size1st = size - size2nd;
            int l = fat32.getRequiredClusters(size1st);
            for (int i = 0; i < l; i++) {
                clusters.add(startCluster + i);
            }
            l = fat32.getRequiredClusters(size2nd);
            for (int i = 0; i < l; i++) {
                clusters.add(lastCluster - l + 1 + i);
            }
            fat.setClusterChain(clusters.toArray(new Integer[clusters.size()]));
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 3: analyze 2nd clusters
     * @param args 0:device, 1:outdir, 2:list file (tsv)
     */
    static void exec5(String[] args) throws Exception {
        Fat32 fat32 = new Fat32(args[0]);
        String file = args[2];

        final int plus = 2000;
        byte[] buffer = new byte[fat32.getBytesPerSector()];
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextInt()) {
            int lastCluster = scanner.nextInt();
            int clusters = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("lastCluster: " + lastCluster + ", clusters: " + clusters + ", size: " + size);

            int c = 0;
            for (int i = lastCluster; i > 2; i--) {
                if (fat32.isUsing(i)) {
                    System.err.print("X");
                    System.err.flush();
                } else {
                    System.err.print("O");
                    System.err.flush();
                    if (c > clusters + plus) {
                        break;
                    }
                    c++;
                }
                if (i % 16 == 0) {
                    System.err.println();
                }
            }
System.err.println();

            c = 0;
            for (int i = lastCluster; i > 2; i--) {
                if (fat32.isUsing(i)) {
System.err.println("cluster: " + i + " used, skip");
                } else {
                    int targetSector = fat32.getSector(i);
                    fat32.readSector(buffer, targetSector);
System.err.println("cluster: " + i + ": " + c + "\n" + StringUtil.getDump(buffer, 128));
                    if (c > clusters + plus) {
                        break;
                    }
                    c++;
                }
            }
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 4: find 2nd clusters from last cluster (continued, clusters not specified), and salvage
     * @param args 0:device, 1:outdir, 2:list file (tsv)
     */
    void exec4(String[] args) throws Exception {
        this.fat32 = new Fat32(args[0]);
        String dir = args[1];
        String file = args[2];

        byte[] buffer = new byte[fat32.getBytesPerSector()];
        File output;
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextInt()) {
            // TSV
            int lastCluster = scanner.nextInt();
            int clusters = scanner.nextInt(); // non sense
            int size = scanner.nextInt(); // full size
System.err.println("lastCluster: " + lastCluster + ", clusters: " + clusters + ", size: " + size);

            List<Integer> clusterList = new ArrayList<>();
            for (int i = lastCluster; i > 2; i--) {
                if (fat32.isUsing(i)) {
System.err.println("\nnot continued, stop");
                    break;
                } else {
                    System.err.print("O");
                    System.err.flush();
                    clusterList.add(0, i);
                    if (clusterList.size() * fat32.getBytesPerCluster() > size) {
                        break;
                    }
                }
            }
System.err.println("createing " + String.valueOf(lastCluster) + ".dat");

//if (false) {
            output = new File(dir, String.valueOf(lastCluster) + ".dat");
            OutputStream os = new FileOutputStream(output);
outer:
            for (int cluster : clusterList) {
                int rest;
                if (cluster == lastCluster) {
System.err.print("last cluster: " + cluster);
                    rest = size % fat32.getBytesPerCluster();
                } else {
System.err.print("cluster: " + cluster);
                    rest = fat32.getBytesPerCluster();
                }
                for (int sector = 0; sector < fat32.getSectorsPerCluster(); sector++) {
                    int targetSector = fat32.getSector(cluster) + sector;
                    fat32.readSector(buffer, targetSector);
                    if (rest >= fat32.getBytesPerSector()) {
                        os.write(buffer, 0, fat32.getBytesPerSector());
                        rest -= fat32.getBytesPerSector();
                    } else {
                        os.write(buffer, 0, rest);
                        rest -= rest;
                        break outer;
                    }
                }
System.err.println(" 2nd parts salvaged");
            }

System.err.println(" 2nd parts salvaged, finish: " + ((clusterList.size() - 1) * fat32.getBytesPerCluster() + (size % fat32.getBytesPerCluster())) + " / " + size);
            os.flush();
            os.close();
            output.renameTo(new File(dir, String.valueOf(lastCluster) + ".incomplete"));
//}
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 3: find 2nd clusters from last cluster (uncontinued clusters ok?), and salvage, sure id3v1
     * @param args 0:device, 1:outdir, 2:list file (tsv)
     */
    void exec3(String[] args) throws Exception {
        this.fat32 = new Fat32(args[0]);
        String dir = args[1];
        String file = args[2];

        File output;
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextInt()) {
            int lastCluster = scanner.nextInt();
            int clusters = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("lastCluster: " + lastCluster + ", clusters: " + clusters + ", size: " + size);

            List<Integer> clusterList = new ArrayList<>();
            boolean continued = true;
            for (int i = lastCluster; i > 2; i--) {
                if (fat32.isUsing(i)) {
                    System.err.print("X");
                    System.err.flush();
                } else {
                    System.err.print("O");
                    System.err.flush();
                    clusterList.add(0, i);
                    if (clusterList.size() == clusters) {
                        break;
                    }
                }
            }
System.err.println();

if (false) {
            output = new File(dir, String.valueOf(lastCluster) + ".dat");
            OutputStream os = new FileOutputStream(output);
            int rest = size;
            byte[] buffer = new byte[fat32.getBytesPerCluster()];
            boolean found = true;
outer:
            for (int cluster : clusterList) {
System.err.print("cluster: " + cluster);
                fat32.readCluster(buffer, cluster);
                if (rest > fat32.getBytesPerCluster()) {
                    os.write(buffer, 0, fat32.getBytesPerCluster());
                    rest -= fat32.getBytesPerCluster();
                } else {
                    if (found) {
                        int index = fat32.matcher(buffer).indexOf("TAG".getBytes(), 0);
                        if (index == -1) {
System.err.println(" tag not found: cluster: " + cluster);
                            os.write(buffer, 0, fat32.getBytesPerCluster());
                            rest = fat32.getBytesPerCluster();
                            continue;
                        } else {
System.err.print(" tag found: " + (index + 128) + " / " + rest + ", ");
                            os.write(buffer, 0, (index + 128) % fat32.getBytesPerCluster());
                            rest -= (index + 128) % fat32.getBytesPerCluster();
                            if (rest > 0) {
                                found = true;
                                continue;
                            } else {
                                break outer;
                            }
                        }
                    } else {
                        os.write(buffer, 0, rest);
                        rest -= rest;
                        break outer;
                    }
                }
System.err.println(" 2nd parts salvaged: " + (size - rest) + " / " + size + "\n" + StringUtil.getDump(buffer, 32));
            }

            os.flush();
            os.close();
            if (!continued) {
                output.renameTo(new File(dir, String.valueOf(lastCluster) + ".incomplete"));
            } else {
System.err.println(" 2nd parts salvaged, finish: " + (size - rest) + " / " + size);
System.err.println("cat -B " + dir + "/$1.incomplete " + dir + "/" + String.valueOf(lastCluster) + ".dat > " + dir + "/$1");
            }
}
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 2: find 2nd clusters from last cluster (uncontinued clusters ok?), and salvage
     * @param args 0:device, 1:outdir, 2:list file (tsv)
     */
    void exec2(String[] args) throws Exception {
        this.fat32 = new Fat32(args[0]);
        String dir = args[1];
        String file = args[2];

System.err.println("---- fill deleted clusters");
        setUserCluster();
System.err.println("----");

        byte[] buffer = new byte[fat32.getBytesPerSector()]; 
        File output;
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextInt()) {
            int lastCluster = scanner.nextInt();
            int clusters = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("lastCluster: " + lastCluster + ", clusters: " + clusters + ", size: " + size);

            List<Integer> clusterList = new ArrayList<>();
            boolean continued = true;
            for (int i = lastCluster; i > 2; i--) {
                if (fat32.isUsing(i)) {
                    System.err.print("X");
                    System.err.flush();
                } else {
                    System.err.print("O");
                    System.err.flush();
                    clusterList.add(0, i);
                    if (clusterList.size() == clusters) {
                        break;
                    }
                }
            }
System.err.println();

//if (false) {
            output = new File(dir, String.valueOf(lastCluster) + ".dat");
            OutputStream os = new FileOutputStream(output);
            int rest = size;
outer:
            for (int cluster : clusterList) {
System.err.print("cluster: " + cluster);
                for (int sector = 0; sector < fat32.getSectorsPerCluster(); sector++) {
                    int targetSector = fat32.getSector(cluster) + sector;
                    fat32.readSector(buffer, targetSector);
                    if (rest > fat32.getBytesPerSector()) {
                        os.write(buffer, 0, fat32.getBytesPerSector());
                        rest -= fat32.getBytesPerSector();
                    } else {
                        os.write(buffer, 0, rest);
                        rest -= rest;
                        break outer;
                    }
                }
System.err.println(" 2nd parts salvaged: " + (size - rest) + " / " + size);
            }

            os.flush();
            os.close();
            if (!continued) {
                output.renameTo(new File(dir, String.valueOf(lastCluster) + ".incomplete"));
            } else {
System.err.println(" 2nd parts salvaged, finish: " + (size - rest) + " / " + size);
System.err.println("cat -B " + dir + "/$1.incomplete " + dir + "/" + String.valueOf(lastCluster) + ".dat > " + dir + "/$1");
            }
//}
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 1: find 2nd clusters from last cluster (need continued clusters), and salvage
     * @param args 0:device, 1:outdir, 2:list file (csv)
     */
    void exec1(String[] args) throws Exception {
        this.fat32 = new Fat32(args[0]);
        String dir = args[1];
        String file = args[2];

        byte[] buffer = new byte[fat32.getBytesPerSector()]; 
        File output;
        Scanner scanner = new Scanner(new FileInputStream(file));
        while (scanner.hasNextInt()) {
            int lastCluster = scanner.nextInt();
            int clusters = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("lastCluster: " + lastCluster + ", clusters: " + clusters + ", size: " + size);
            // 0 1 2 3 4 5
            //         *   3
            //     + + +
            boolean continued = true;
            for (int i = lastCluster; i > lastCluster - clusters; i--) {
                if (fat32.isUsing(i)) {
System.err.println("not continued: " + lastCluster);
                    continued = false;
                    break;
                }
            }

//if (false) {
            output = new File(dir, String.valueOf(lastCluster) + ".dat");
            OutputStream os = new FileOutputStream(output);
            int rest = size;
outer:
            for (int cluster = lastCluster - clusters + 1; cluster <= lastCluster; cluster++) {
System.err.print("cluster: " + cluster);
                for (int sector = 0; sector < fat32.getSectorsPerCluster(); sector++) {
                    int targetSector = fat32.getSector(cluster) + sector;
                    fat32.readSector(buffer, targetSector);
                    if (rest > fat32.getBytesPerSector()) {
                        os.write(buffer, 0, fat32.getBytesPerSector());
                        rest -= fat32.getBytesPerSector();
                    } else {
                        os.write(buffer, 0, rest);
                        rest -= rest;
                        break outer;
                    }
                }
System.err.println(" 2nd parts salvaged: " + (size - rest) + " / " + size);
            }

            os.flush();
            os.close();
            if (!continued) {
                output.renameTo(new File(dir, String.valueOf(lastCluster) + ".incomplete"));
            } else {
System.err.println(" 2nd parts salvaged, finish: " + (size - rest) + " / " + size);
System.err.println("cat -B " + dir + "/$1.incomplete " + dir + "/" + String.valueOf(lastCluster) + ".dat > " + dir + "/$1");
            }
//}
        }
        scanner.close();
    }
}

/* */
