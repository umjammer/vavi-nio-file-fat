/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.DataInput;
import java.io.IOException;

/** */
class DeletedLongNameDirectoryEntry extends LongNameDirectoryEntry {
    /** */
    private static final long serialVersionUID = -3509895194089903860L;

    DeletedLongNameDirectoryEntry(DataInput is) throws IOException {
        super(is);
        subEntryNo = -1;
        isLast = false;
    }
}
/* */
