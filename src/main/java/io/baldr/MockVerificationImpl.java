package io.baldr;

import java.util.function.Consumer;

class MockVerificationImpl<T> implements MockVerification<T> {
    T on;
    private MockInvocation<?> previousMatch;

    public MockVerificationImpl(T t, Consumer<T> consumer) {
        on = t;
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }
        called(consumer);
    }

    private void called(Consumer<T> consumer) {
        int previousOrder = -1;
        if (previousMatch != null) {
            previousOrder = previousMatch.getOrder();
        }
        MockInvocations mockInvocations = ((MockedObject<?>) on).$getInvocations();
        mockInvocations.enterVerificationMode();
        consumer.accept(on);
        mockInvocations.exitVerificationMode();
        MockInvocation<?> currentMatch = mockInvocations.getPreviousMatch();
        if(previousOrder > -1 && previousOrder > currentMatch.getOrder()) {
            throw new MockVerificationException(previousMatch.toString()+" was expected to be called before "+ currentMatch);
        }
        previousMatch = currentMatch;
    }

    @Override
    public void thenCalled(Consumer<T> consumer) {
        called(consumer);
    }
}
