package io.baldr;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MockContext {
    private static InheritableThreadLocal<MockContext> context =  new InheritableThreadLocal<>();
    static {
        context.set(new MockContext());
    }


    private InvocationModeEnum invocationMode = InvocationModeEnum.Invoke;
    private ConcurrentLinkedDeque<MockInvocation<?>> invocations = new ConcurrentLinkedDeque<>();

    public static MockContext get() {
        return context.get();
    }

    public <T> void addInvocation(MockInvocation<T> invocation) {
        invocations.add(invocation);
    }

    public void enterAssert() {
        invocations.clear();
        invocationMode = InvocationModeEnum.Assert;
    }

    public void enterStubbing() {
        invocations.clear();
        invocationMode = InvocationModeEnum.Stubbing;
    }

    public void exitAssert() {
        invocations.clear();
        invocationMode = InvocationModeEnum.Invoke;
    }

    public void exitStubbing() {
        invocations.clear();
        invocationMode = InvocationModeEnum.Invoke;
    }

    public InvocationMode getInvocationMode(MockShadow mockShadow) {
        return invocationMode.get(mockShadow);
    }

    public Optional<MockInvocation> getPrevious() {
        return Optional.ofNullable(invocations.peekLast());
    }
}
