package io.baldr;

class NeverVerificationSetup implements InvocationMode {
    private final MockShadow mockShadow;

    NeverVerificationSetup(MockShadow mockShadow) {
        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {}

    @Override
    public InvocationResult<Object> finish(MockInvocation<?> invocation) {
        InvocationResult<MockInvocation> match = mockShadow.popMatchingInvocation(invocation);
        if (match.isPresent()) {
            throw new MockVerificationException(mockShadow.getCurrent() + " was not expected to be called but was");
        }
        return InvocationResult.empty();
    }
}
