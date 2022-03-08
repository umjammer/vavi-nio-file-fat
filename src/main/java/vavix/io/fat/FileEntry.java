/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.util.Collection;


/**
 * FileEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public interface FileEntry extends DirectoryEntry, Comparable<FileEntry> {
    boolean isDirectory();

    String getName();

    int getStartCluster();

    void setLongName(Collection<LongNameDirectoryEntry> longNames);

    long length();

    long lastModified();
}

/* */
