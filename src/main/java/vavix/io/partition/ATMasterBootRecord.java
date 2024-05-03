/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.partition;

import vavi.util.serdes.Element;
import vavi.util.serdes.Serdes;


/**
 * ATMasterBootRecord.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
@Serdes(bigEndian = false)
public class ATMasterBootRecord {
    /** プログラム */
    @Element(sequence = 1)
    byte[] bootStrapCode = new byte[446];

    /** 領域情報 */
    @Element(sequence = 2)
    public ATPartitionEntry[] partitionEntries = new ATPartitionEntry[4];

    /** 0xaa55 le */
    @Element(sequence = 3, validation = "new byte[] { 0x55, 0xaa }")
    byte[] bootSignature = new byte[2];
}
