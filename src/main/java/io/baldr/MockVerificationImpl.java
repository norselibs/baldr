package io.baldr;

import io.ran.CrudRepository;

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
        MockShadow mockShadow = ((MockedObject<?>) on).$getShadow();
        consumer.accept(instance);

        MockInvocation<?> currentMatch = mockShadow.getMatchingInvocation();

        if (previous != null && currentMatch != null) {
            int previousOrder = previous.getOrder();
            int currentOrder = currentMatch.getOrder();
            if (previousOrder > -1 && previousOrder > currentOrder) {
                throw new MockVerificationException(previous.toString() + " was expected to be called before " + currentMatch);
            }
        }
        this.previous = currentMatch;

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
        return new MockVerificationImpl<>(c, consumer, this.previous);
    }
}
