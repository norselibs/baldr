package io.baldr;

import java.util.function.Consumer;
import java.util.function.Function;

public interface MockVerification<T> {
    MockVerification<T> thenCalled(Consumer<T> consumer);

    <C> MockVerification<C> thenCalled(C c, Consumer<C> consumer);

    <N> MockVerification<N> then(Function<T, N> function);

    MockVerification<T> times(int n);
}
