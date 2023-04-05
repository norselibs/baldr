package io.baldr;

import java.util.Optional;
import java.util.function.Consumer;

class MockVerificationImpl<T> implements MockVerification<T> {
    private Object on;
    private MockInvocation previous;

    public MockVerificationImpl(T t, Consumer<T> consumer, MockInvocation previous) {
        on = t;
        this.previous = previous;
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }
        called(on, consumer);
    }

    private void called(Object instance, Consumer consumer) {
        MockContext.get().setCurrentVerificationImpl(this);
        consumer.accept(instance);
    }

    @Override
    public void thenCalled(Consumer<T> consumer) {
        try {
            MockContext.get().enterAssert();
            called(on, consumer);
        } finally {
            MockContext.get().exitAssert();
        }
    }

    @Override
    public <C> MockVerification<C> thenCalled(C c, Consumer<C> consumer) {
        try {
            MockContext.get().enterAssert();
            return new MockVerificationImpl<>(c, consumer, this.previous);
        } finally {
            MockContext.get().exitAssert();
        }
    }

    public Optional<MockInvocation> getPrevious() {
        return Optional.ofNullable(previous);
    }

    public void setPrevious(MockInvocation mockInvocation) {
        this.previous = mockInvocation;
    }
}
