/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.util.List;


/**
 * DirectoryEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public interface DirectoryEntry extends FileEntry {

    /** */
    List<FileEntry> entries();

    /** */
    void setEntries(List<FileEntry> entries);

    /**
     * Finds a {@link FileEntry} named <code>filename</code> contained in <code>this</code>
     * @param filename filename not path
     */
    FileEntry find(String filename);
}
