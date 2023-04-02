package io.baldr;

import io.ran.Clazz;

import java.util.Collections;
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
    public Optional<Object> finish(MockInvocation invocation) {
        Optional<MockInvocation<?>> match = invocation.matchesAny(mockShadow.getInvocations());
        if(match.isEmpty()) {
            throw new MockVerificationException("No matching invocations of "+ mockShadow.getCurrent().toString()+" invoked on mock");
        }
        return Optional.empty();

    }
}
