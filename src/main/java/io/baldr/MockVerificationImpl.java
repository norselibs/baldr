package io.baldr;

import io.ran.Clazz;

import java.util.function.Consumer;

class MockVerificationImpl<T> implements MockVerification {
    public MockVerificationImpl(T t, Consumer<T> consumer) {
        if (!(t instanceof MockedObject)) {
            throw new RuntimeException(t.getClass().getName()+" must be a mock");
        }
        ((MockedObject<?>) t).$getInvocations().enterVerificationMode();
        consumer.accept(t);
        ((MockedObject<?>) t).$getInvocations().exitVerificationMode();
    }
}
