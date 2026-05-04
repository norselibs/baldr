package io.baldr;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class On<T> {
    final T mock;

    On(T mock) {
        if (!(mock instanceof MockedObject)) {
            throw new RuntimeException(mock.getClass().getName() + " must be a mock");
        }
        this.mock = mock;
    }

    public <R> Chain<T, R> when(Function<T, R> fn) {
        return new Chain<>(mock, fn);
    }

    public MockVerification<T> assertCalled(Consumer<T> fn) {
        MockContext.get().enterAssert();
        try {
            return new MockVerificationImpl<>(mock, fn, null);
        } finally {
            MockContext.get().exitAssert();
        }
    }

    public void assertNeverCalled(Consumer<T> fn) {
        MockContext.get().enterNeverAssert();
        try {
            fn.accept(mock);
        } finally {
            MockContext.get().exitAssert();
        }
    }
}
