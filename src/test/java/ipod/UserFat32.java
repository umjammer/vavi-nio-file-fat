/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ipod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vavix.io.fat.BiosParameterBlock;
import vavix.io.fat.Fat;


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
    @Override
    public Integer[] getClusterChain(int cluster) throws IOException {
        List<Integer> clusters = new ArrayList<>();
        do {
            clusters.add(cluster);
            cluster = this.clusters[cluster];
        } while (0x0000_0002 <= cluster && cluster <= 0x0fff_fff6);
        return clusters.toArray(new Integer[0]);
    }
    @Override
    public boolean isUsing(int cluster) throws IOException {
        int value = getClusterValue(cluster);
        return 0x0000_0002 <= value && value <= 0x0fff_ffff;
    }
    @Override
    public void setClusterValue(int cluster, int value) throws IOException {
        clusters[cluster] = value;
    }
    @Override
    public int getClusterValue(int cluster) throws IOException {
        return clusters[cluster];
    }
    @Override
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
