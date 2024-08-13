package io.baldr;

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
    public InvocationResult<Object> finish(MockInvocation invocation) {
        mockShadow.addStub(invocation);

        return InvocationResult.empty();
    }
}
