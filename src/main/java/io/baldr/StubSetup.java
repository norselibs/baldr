package io.baldr;

import io.ran.Clazz;

import java.util.Collections;
import java.util.Optional;

import static io.baldr.Baldr.mock;

public class StubSetup implements InvocationMode {
    private MockShadow mockShadow;

    public StubSetup(MockShadow mockShadow) {
        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {

    }

    @Override
    public Optional<Object> finish(MockInvocation invocation) {
        mockShadow.addStub(invocation);

        return Optional.empty();
    }
}
