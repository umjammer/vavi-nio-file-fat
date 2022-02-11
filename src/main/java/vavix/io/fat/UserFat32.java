/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** */
public class UserFat32 implements Fat {
    /** */
    private static final long serialVersionUID = -6771019237363727447L;
    /** */
    int[] clusters;
    /** */
    public UserFat32(BiosParameterBlock bpb, Fat fat) throws IOException {
        int size = bpb.getLastCluster();
        clusters = new int[size];
        for (int i = 0; i < size; i++) {
            clusters[i] = fat.getClusterValue(i);
        }
    }
    /** */
    public Integer[] getClusterChain(int cluster) throws IOException {
        List<Integer> clusters = new ArrayList<>();
        do {
            clusters.add(cluster);
            cluster = this.clusters[cluster];
        } while (0000_0002 <= cluster && cluster <= 0x0fff_fff6);
        return clusters.toArray(new Integer[clusters.size()]);
    }
    /** */
    public boolean isUsing(int cluster) throws IOException {
        int value = getClusterValue(cluster);
        return 0000_0002 <= value && value <= 0x0fff_ffff;
    }
    /** */
    public void setClusterValue(int cluster, int value) throws IOException {
        clusters[cluster] = value;
    }
    /** */
    public int getClusterValue(int cluster) throws IOException {
        return clusters[cluster];
    }
    /** */
    public void setClusterChain(Integer[] clusters) throws IOException {
        for (int i = 0; i < clusters.length; i++) {
            if (i == clusters.length - 1) {
                clusters[i] = 0x0fff_ffff;
            } else {
                clusters[i] = clusters[i + 1];
            }
        }
    }
}
/* */
