/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.DataInput;
import java.io.IOException;
import java.util.Collection;


/**
 * DeletedLongNameFileEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/29 nsano initial version <br>
 */
class DeletedLongNameFileEntry extends LongNameFileEntry implements DeletedEntry {

    /** */
    private static final long serialVersionUID = -3509895194089903860L;

    DeletedLongNameFileEntry(DataInput is) throws IOException {
        super(is);
        subEntryNo = -1;
        isLast = false;
    }

    @Override
    public boolean isStartClusterValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public int getStartCluster() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLongName(Collection<LongNameFileEntry> longNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public long created() {
        return 0;
    }

    @Override
    public long lastAccessed() {
        return 0;
    }
}

/* */
