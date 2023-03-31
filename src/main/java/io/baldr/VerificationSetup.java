package io.baldr;

import java.util.Optional;

public class VerificationSetup implements InvocationMode {
    private MockShadow mockShadow;

    public VerificationSetup(MockShadow mockShadow) {

        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {

    }

    @Override
    public Object finish() {
        Optional<MockInvocation<?>> match = mockShadow.getCurrent().matchesAny(mockShadow.getInvocations());
        if(match.isEmpty()) {
            throw new MockVerificationException("No matching invocations of "+ mockShadow.getCurrent().toString()+" invoked on mock");
        } else {
            mockShadow.setPreviousMatch(match.get());
        }

        mockShadow.resetCurrent();
        return null;
    }
}
