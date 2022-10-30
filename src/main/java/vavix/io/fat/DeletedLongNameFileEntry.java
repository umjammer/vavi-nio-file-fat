/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.DataInput;
import java.io.IOException;

/**
 * DeletedLongNameFileEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/29 nsano initial version <br>
 */
class DeletedLongNameFileEntry extends LongNameFileEntry {

    /** */
    private static final long serialVersionUID = -3509895194089903860L;

    DeletedLongNameFileEntry(DataInput is) throws IOException {
        super(is);
        subEntryNo = -1;
        isLast = false;
    }
}

/* */
