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
public interface FileEntry {

    boolean isDirectory();

    String getName();

    int getStartCluster();

    void setLongName(Collection<LongNameFileEntry> longNames);

    /** file size */
    long length();

    long lastModified();

    long created();

    long lastAccessed();
}
