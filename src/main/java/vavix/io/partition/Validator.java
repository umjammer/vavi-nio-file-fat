/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.partition;

import java.lang.System.Logger;


/**
 * boot sector value validator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-11-30 nsano initial version <br>
 */
public interface Validator {

    /** logger */
    Logger logger = System.getLogger(Validator.class.getName());

    /** validation priority */
    int weight();

    /** use this validator nor not */
    boolean enabled();

    /** do validation */
    boolean validate(byte[] firstSectors);
}
