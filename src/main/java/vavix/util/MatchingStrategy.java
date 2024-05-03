/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util;


/**
 * MatchingStrategy.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/03 umjammer initial version <br>
 */
public interface MatchingStrategy<S, P> {

    int indexOf(S source, P pattern);
}
