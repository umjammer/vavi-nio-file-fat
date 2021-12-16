/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util;


/**
 * StrategyPatternMatcher.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/03 umjammer initial version <br>
 */
public class StrategyPatternMatcher<S, P extends MatchingStrategy<S, ?>> implements Matcher<P> {

    private S source;

    public StrategyPatternMatcher(S source) {
        this.source = source;
    }

    public int indexOf(P matchingStrategy, int fromIndex) {
        return matchingStrategy.indexOf(source, null);
    }
}

/* */
