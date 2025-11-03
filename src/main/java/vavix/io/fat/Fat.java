/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.IOException;
import java.io.Serializable;


/**
 * Fat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
public interface Fat extends Serializable {
    /**
     * @param cluster startCluster
     */
    Integer[] getClusterChain(int cluster) throws IOException;

    /** */
    boolean isUsing(int cluster) throws IOException;

    /** */
    void setClusterValue(int cluster, int value) throws IOException;

    /** */
    int getClusterValue(int cluster) throws IOException;

    /** */
    void setClusterChain(Integer[] clusters) throws IOException;
}
