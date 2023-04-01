package io.baldr;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public class MockShadow {
    ConcurrentLinkedQueue<MockInvocation<?>> stubs = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<MockInvocation<?>> invocations = new ConcurrentLinkedQueue<>();

    private InvocationMode invocationMode;
    private MockInvocation<?> currentInvocation;
    private static final AtomicInteger invocationOrder = new AtomicInteger(0);
    private MockInvocation<?> previousMatch;
    private Object activeStub = null;

    public MockShadow() {
        invocationMode = new Invoke(this);
    }

    public static MockShadow get() {
        return new MockShadow();
    }

    public <T> MockInvocation<T> buildInvocation(T on,String methodName) {
        MockInvocation<T> invocation = new MockInvocation<>(this, on, methodName);
        invocationMode.build(invocation);
        currentInvocation =  invocation;

        return invocation;
    }

    public void enterVerificationMode() {
        invocationMode = new VerificationSetup(this);
    }

    public void exitVerificationMode() {
        invocationMode = new Invoke(this);
    }

    public Object finish() {
        return invocationMode.finish();
    }

    public MockInvocation<?> getPreviousMatch() {
        return previousMatch;
    }

    public void enterStubbingMode() {
        invocationMode = new StubSetup(this);
    }

    public void exitStubbingMode() {
        invocationMode = new Invoke(this);
    }

    public MockInvocation<?> getCurrent() {
        return currentInvocation;
    }

    public MockedObject<?> getActiveStub() {
        return (MockedObject<?>) activeStub;
    }

    public int incrementOrder() {
        return invocationOrder.incrementAndGet();
    }

    public <T> void addInvocation(MockInvocation<T> invocation) {
        invocations.add(invocation);
    }

    public Queue<MockInvocation<?>> getInvocations() {
        return invocations;
    }

    public void setPreviousMatch(MockInvocation<?> mockInvocation) {
        previousMatch = mockInvocation;
    }

    public void resetCurrent() {
        currentInvocation = null;
    }

    public void addStub(MockInvocation<?> current) {
        stubs.add(current);
    }

    public void setActiveStub(Object stub) {
        activeStub = stub;
    }

    public ConcurrentLinkedQueue<MockInvocation<?>> getStubs() {
        return stubs;
    }
}
