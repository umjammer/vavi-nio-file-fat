/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.DataInput;
import java.io.IOException;


/**
 * DeletedEntryImpl.
 * {@link FileEntryImpl#lastAccessed()} is the date deleted.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/31 nsano initial version <br>
 */
public class DeletedEntryImpl extends FileEntryImpl implements DeletedEntry {

    /** */
    private static final long serialVersionUID = -8752690030998809470L;

    /** */
    private boolean startClusterValid = false;

    /** */
    protected String getPrefixString() {
        return "_";
    }

    /** */
    public DeletedEntryImpl(DataInput is) throws IOException {
        super(is);
    }

    /** */
    public boolean isStartClusterValid() {
        return startClusterValid;
    }

    /** */
    public void setStartClusterValid(boolean startClusterValid) {
        this.startClusterValid = startClusterValid;
    }
}
