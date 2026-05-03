package io.baldr;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes","unchecked"})
class MockVerificationImpl<T> implements MockVerification<T> {
    private final Object on;
    private MockInvocation previous;
    private MockInvocation lastTemplate;

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
        lastTemplate = ((MockedObject) instance).$getShadow().getCurrent();
    }

    public MockVerification<T> thenCalled(Consumer<T> consumer) {
        try {
            MockContext.get().enterAssert();
            called(on, consumer);
        } finally {
            MockContext.get().exitAssert();
        }
        return this;
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

    @Override
    public MockVerification<T> times(int n) {
        MockShadow shadow = ((MockedObject) on).$getShadow();
        for (int i = 1; i < n; i++) {
            InvocationResult<MockInvocation> match = shadow.popMatchingInvocation(lastTemplate);
            if (!match.isPresent()) {
                throw new MockVerificationException(
                    lastTemplate + " expected to be called " + n + " times but was only called " + i + " time(s)");
            }
        }
        InvocationResult<MockInvocation> extra = shadow.popMatchingInvocation(lastTemplate);
        if (extra.isPresent()) {
            throw new MockVerificationException(
                lastTemplate + " expected to be called exactly " + n + " times but was called more than " + n + " time(s)");
        }
        return this;
    }

    public Optional<MockInvocation> getPrevious() {
        return Optional.ofNullable(previous);
    }

    public void setPrevious(MockInvocation mockInvocation) {
        this.previous = mockInvocation;
    }
}
