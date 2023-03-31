package io.baldr;

import java.util.function.Consumer;

public interface MockVerification<T> {
    void thenCalled(Consumer<T> consumer);
}
