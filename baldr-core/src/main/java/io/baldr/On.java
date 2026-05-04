package io.baldr;

import java.util.function.Function;

public class On<T> {
    final T mock;

    On(T mock) { this.mock = mock; }

    public <R> Chain<T, R> when(Function<T, R> fn) {
        return new Chain<>(mock, fn);
    }
}
