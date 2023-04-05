package io.baldr;

import io.ran.Clazz;

import java.util.Collections;
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
    private Object mockObject;

    public MockShadow(Object mockObject) {
        this.mockObject = mockObject;
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

    public Object finish(MockInvocation invocation) {
        Optional<Object> ret = invocationMode().finish(invocation);
        return ret.orElseGet(() -> {
            Clazz<?> returnType = Clazz.of(getCurrent().getMethod().getReturnType());
            if (returnType.isPrimitive() || returnType.isBoxedPrimitive()) {
                return returnType.getDefaultValue();
            } else if (returnType.is(Clazz.of(String.class), Collections.emptySet())) {
                return null;
            } else {
                return getReturnMock(invocation);
            }
        });
    }

    public Optional<MockInvocation> popMatchingInvocation(MockInvocation invocation) {
        Optional<MockInvocation> matching = invocations.stream().filter(mi -> mi.matches(invocation)).findFirst();
        if (matching.isPresent()) {
            invocations.remove(matching.get());
        }
        return matching;
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
        return mockedReturns.computeIfAbsent(invocation, m -> (MockedObject<?>) mock(invocation.getMethod().getReturnType()));
    }

    public void addStub(MockInvocation invocation) {
        stubs.add(invocation);
    }
}
