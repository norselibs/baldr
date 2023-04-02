package io.baldr.hamcrest;

import org.hamcrest.Matcher;

public interface BaldrMatcher {
    void setMatcher(Matcher<?> matcher);
}
