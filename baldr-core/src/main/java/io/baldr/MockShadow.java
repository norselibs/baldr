package io.baldr;

import io.ran.Clazz;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static io.baldr.Baldr.mock;

@SuppressWarnings("rawtypes")
public class MockShadow {
    ConcurrentLinkedQueue<MockInvocation> stubs = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<MockInvocation> invocations = new ConcurrentLinkedQueue<>();
    ConcurrentHashMap<MockInvocation, MockedObject> mockedReturns = new ConcurrentHashMap<>();

    private MockInvocation currentInvocation;
    private static final AtomicInteger invocationOrder = new AtomicInteger(0);
    private Object activeMock = null;
    private MockedObject mockObject;

    public MockShadow(Object mockObject) {
        this.mockObject = (MockedObject) mockObject;
    }

    public static MockShadow get(Object mockObject) {
        return new MockShadow(mockObject);
    }

    public <T> MockInvocation<T> buildInvocation(T on,String methodName) {
        MockInvocation<T> invocation = new MockInvocation<>(this, on, methodName);
        invocationMode().build(invocation);
        currentInvocation =  invocation;
        MockContext.get().addInvocation(invocation);
        return invocation;
    }

    private InvocationMode invocationMode() {
        return MockContext.get().getInvocationMode(this);
    }

    public InvocationResult<Object> finish(MockInvocation invocation) {
        InvocationResult<Object> ret = invocationMode().finish(invocation);
        if (ret.isPresent()) {
            return ret;

        } else {
            Clazz<?> returnType = Clazz.of(getCurrent().getMethod().getReturnType());
            if (returnType.isVoid()) {
                return InvocationResult.of(null);
            } else if (returnType.isPrimitive() || returnType.isBoxedPrimitive()) {
                if (mockObject instanceof SpiedObject<?>) {
                    return InvocationResult.empty();
                }
                return InvocationResult.of(returnType.getDefaultValue());
            } else if (returnType.clazz.isAssignableFrom(String.class)) {
                return InvocationResult.of("");
            } else {
                if (mockObject instanceof SpiedObject<?>) {
                    return InvocationResult.empty();
                }

                return InvocationResult.of(getReturnMock(invocation));
            }
        }
    }

    public InvocationResult<MockInvocation> popMatchingInvocation(MockInvocation invocation) {
        Optional<MockInvocation> matching = invocations.stream().filter(mi -> mi.matches(invocation)).findFirst();
        if (matching.isPresent()) {
            invocations.remove(matching.get());
            return InvocationResult.of(matching.get());
        }
        return InvocationResult.empty();
    }

    public MockInvocation<?> getCurrent() {
        return currentInvocation;
    }

    public MockedObject<?> getActiveMock() {
        return (MockedObject<?>) activeMock;
    }

    public int incrementOrder() {
        return invocationOrder.incrementAndGet();
    }

    public <T> void addInvocation(MockInvocation<T> invocation) {
        invocations.add(invocation);
    }

    public Queue<MockInvocation> getInvocations() {
        return invocations;
    }

    public ConcurrentLinkedQueue<MockInvocation> getStubs() {
        return stubs;
    }

    public MockedObject<?> getReturnMock(MockInvocation invocation) {
        return mockedReturns.computeIfAbsent(invocation, m -> (MockedObject<?>) Baldr.mock(invocation.getMethod().getReturnType()));
    }

    public String getName() {
        return this.mockObject.getClass().getSuperclass().getSimpleName();
    }

    public void addStub(MockInvocation invocation) {
        stubs.add(invocation);
    }
}
