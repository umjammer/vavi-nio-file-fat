/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

/**
 * DeletedEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/11/03 umjammer initial version <br>
 */
public interface DeletedEntry extends FileEntry {

    /** */
    boolean isStartClusterValid();
}

/* */
