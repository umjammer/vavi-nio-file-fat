/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ipod;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import vavi.util.StringUtil;
import vavix.io.WinRawIO;
import vavix.io.fat.Fat;
import vavix.io.fat.FileAllocationTable;
import vavix.util.ByteArrayMatcher;
import vavix.util.Matcher;


/**
 * fat32 forensic 4.
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
    FileAllocationTable fat32;

    /** */
    void setUserCluster() throws Exception {
        Fat fat = new UserFat32(fat32.bpb, fat32.fat);
        fat32.setFat(fat);
        //
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get("uc1.uc")));
        while (scanner.hasNextInt()) {
            // TSV
            int startCluster = scanner.nextInt();
            int size = scanner.nextInt();
System.err.println("startCluster: " + startCluster + ", size: " + size);
            List<Integer> clusters = new ArrayList<>(); 
            for (int i = 0; i < fat32.getRequiredClusters(size); i++) {
                clusters.add(startCluster + i);
            }
            fat.setClusterChain(clusters.toArray(new Integer[0]));
        } 
        scanner.close();
        //
        scanner = new Scanner(Files.newInputStream(Paths.get("uc2.uc")));
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
            fat.setClusterChain(clusters.toArray(new Integer[0]));
        }
        scanner.close();
    }

    //-------------------------------------------------------------------------

    /**
     * 3: analyze 2nd clusters
     * @param args 0:device, 1:outdir, 2:list file (tsv)
     */
    static void exec5(String[] args) throws Exception {
        FileAllocationTable fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        String file = args[2];

        final int plus = 2000;
        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()];
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(file)));
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
                    int targetSector = fat32.bpb.toSector(i);
                    fat32.io().readSector(buffer, targetSector);
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
        this.fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        String dir = args[1];
        String file = args[2];

        int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();

        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()];
        File output;
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(file)));
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
                    if (clusterList.size() * bytesPerCluster > size) {
                        break;
                    }
                }
            }
System.err.println("createing " + String.valueOf(lastCluster) + ".dat");

//if (false) {
            output = new File(dir, String.valueOf(lastCluster) + ".dat");
            OutputStream os = Files.newOutputStream(output.toPath());
outer:
            for (int cluster : clusterList) {
                int rest;
                if (cluster == lastCluster) {
System.err.print("last cluster: " + cluster);
                    rest = size % bytesPerCluster;
                } else {
System.err.print("cluster: " + cluster);
                    rest = bytesPerCluster;
                }
                for (int sector = 0; sector < fat32.bpb.getSectorsPerCluster(); sector++) {
                    int targetSector = fat32.bpb.toSector(cluster) + sector;
                    fat32.io().readSector(buffer, targetSector);
                    if (rest >= fat32.bpb.getBytesPerSector()) {
                        os.write(buffer, 0, fat32.bpb.getBytesPerSector());
                        rest -= fat32.bpb.getBytesPerSector();
                    } else {
                        os.write(buffer, 0, rest);
                        rest -= rest;
                        break outer;
                    }
                }
System.err.println(" 2nd parts salvaged");
            }

System.err.println(" 2nd parts salvaged, finish: " + ((clusterList.size() - 1) * bytesPerCluster + (size % bytesPerCluster)) + " / " + size);
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
    @SuppressWarnings("unused")
    void exec3(String[] args) throws Exception {
        this.fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        String dir = args[1];
        String file = args[2];

        File output;
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(file)));
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
            int bytesPerCluster = fat32.bpb.getSectorsPerCluster() * fat32.bpb.getBytesPerSector();
            output = new File(dir, String.valueOf(lastCluster) + ".dat");
            OutputStream os = Files.newOutputStream(output.toPath());
            int rest = size;
            byte[] buffer = new byte[bytesPerCluster];
            boolean found = true;
outer:
            for (int cluster : clusterList) {
System.err.print("cluster: " + cluster);
                fat32.readCluster(buffer, cluster);
                if (rest > bytesPerCluster) {
                    os.write(buffer, 0, bytesPerCluster);
                    rest -= bytesPerCluster;
                } else {
                    if (found) {
                        Matcher<byte[]> matcher = new ByteArrayMatcher(buffer);
                        int index = matcher.indexOf("TAG".getBytes(), 0);
                        if (index == -1) {
System.err.println(" tag not found: cluster: " + cluster);
                            os.write(buffer, 0, bytesPerCluster);
                            rest = bytesPerCluster;
                            continue;
                        } else {
System.err.print(" tag found: " + (index + 128) + " / " + rest + ", ");
                            os.write(buffer, 0, (index + 128) % bytesPerCluster);
                            rest -= (index + 128) % bytesPerCluster;
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
                output.renameTo(new File(dir, lastCluster + ".incomplete"));
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
        this.fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        String dir = args[1];
        String file = args[2];

System.err.println("---- fill deleted clusters");
        setUserCluster();
System.err.println("----");

        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()]; 
        File output;
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(file)));
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
            OutputStream os = Files.newOutputStream(output.toPath());
            int rest = size;
outer:
            for (int cluster : clusterList) {
System.err.print("cluster: " + cluster);
                for (int sector = 0; sector < fat32.bpb.getSectorsPerCluster(); sector++) {
                    int targetSector = fat32.bpb.toSector(cluster) + sector;
                    fat32.io().readSector(buffer, targetSector);
                    if (rest > fat32.bpb.getBytesPerSector()) {
                        os.write(buffer, 0, fat32.bpb.getBytesPerSector());
                        rest -= fat32.bpb.getBytesPerSector();
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
                output.renameTo(new File(dir, lastCluster + ".incomplete"));
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
        this.fat32 = new FileAllocationTable(new WinRawIO(args[0]));
        String dir = args[1];
        String file = args[2];

        byte[] buffer = new byte[fat32.bpb.getBytesPerSector()]; 
        File output;
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(file)));
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
            output = new File(dir, lastCluster + ".dat");
            OutputStream os = Files.newOutputStream(output.toPath());
            int rest = size;
outer:
            for (int cluster = lastCluster - clusters + 1; cluster <= lastCluster; cluster++) {
System.err.print("cluster: " + cluster);
                for (int sector = 0; sector < fat32.bpb.getSectorsPerCluster(); sector++) {
                    int targetSector = fat32.bpb.toSector(cluster) + sector;
                    fat32.io().readSector(buffer, targetSector);
                    if (rest > fat32.bpb.getBytesPerSector()) {
                        os.write(buffer, 0, fat32.bpb.getBytesPerSector());
                        rest -= fat32.bpb.getBytesPerSector();
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
                output.renameTo(new File(dir, lastCluster + ".incomplete"));
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
