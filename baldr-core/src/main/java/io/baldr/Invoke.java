package io.baldr;

import java.util.Optional;

public class Invoke implements InvocationMode {
    private MockShadow mockShadow;

    public Invoke(MockShadow mockShadow) {
        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {
        invocation.setOrder(mockShadow.incrementOrder());
        mockShadow.addInvocation(invocation);
    }

    @Override
    public Optional<Object> finish(MockInvocation invocation) {
        Optional<MockInvocation> stub = invocation.getMockShadow().getStubs().stream().filter(s -> s.matches(invocation)).findFirst();
        if (stub.isPresent()) {
            return Optional.ofNullable(stub.get().popReturnValue());
        } else {
            return Optional.empty();
        }

    }
}
