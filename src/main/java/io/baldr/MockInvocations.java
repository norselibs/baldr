package io.baldr;

import io.ran.Clazz;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.baldr.Baldr.mock;

@SuppressWarnings("rawtypes")
public class MockInvocations {
    ConcurrentLinkedQueue<MockInvocation<?>> stubs = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<MockInvocation<?>> invocations = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean verificationMode = new AtomicBoolean(false);
    private final AtomicBoolean stubbingMode = new AtomicBoolean(false);
    private MockInvocation<?> currentInvocation;
    private final AtomicInteger invocationOrder = new AtomicInteger(0);
    private MockInvocation<?> previousMatch;
    private Object previousRecursiveStub = null;

    public static MockInvocations get() {
        return new MockInvocations();
    }

    public <T> MockInvocation<T> buildInvocation(T on,String methodName) {
        MockInvocation<T> invocation = new MockInvocation<>(this, on, methodName);
        if (!verificationMode.get() && !stubbingMode.get()) {
            invocation.setOrder(invocationOrder.incrementAndGet());
            invocations.add(invocation);
        }
        currentInvocation =  invocation;

        return invocation;
    }

    public void enterVerificationMode() {
        verificationMode.set(true);
    }

    public void exitVerificationMode() {
        verificationMode.set(false);
    }

    public Object finish() {
        if (verificationMode.get()) {
            Optional<MockInvocation<?>> match = currentInvocation.matchesAny(invocations);
            if(match.isEmpty()) {
                throw new MockVerificationException("No matching invocations of "+ currentInvocation.toString()+" invoked on mock");
            } else {
                previousMatch = match.get();
            }

            currentInvocation = null;
            return null;
        } else if (stubbingMode.get()) {
            stubs.add(currentInvocation);
            Clazz<?> returnType = Clazz.of(currentInvocation.getMethod().getReturnType());
            if (returnType.isPrimitive() || returnType.isBoxedPrimitive()) {
                return returnType.getDefaultValue();
            } else if (returnType.is(Clazz.of(String.class), Collections.emptySet())) {
                return null;
            } else {
                previousRecursiveStub = mock(returnType.clazz);
                exitStubbingMode();
                ((MockedObject<?>)previousRecursiveStub).$getInvocations().enterStubbingMode();

                currentInvocation.addReturnValue(previousRecursiveStub);
                return previousRecursiveStub;
            }
        } else {
            Optional<MockInvocation<?>> stub = stubs.stream().filter(s -> s.matches((MockInvocation)currentInvocation)).findFirst();
            if (stub.isPresent()) {
                return stub.get().popReturnValue();
            } else {
                Clazz<?> returnType = Clazz.of(currentInvocation.getMethod().getReturnType());
                if (returnType.isPrimitive() || returnType.isBoxedPrimitive()) {
                    return returnType.getDefaultValue();
                } else {
                    return mock(returnType.clazz);
                }
            }
        }
    }

    public MockInvocation<?> getPreviousMatch() {
        return previousMatch;
    }

    public void enterStubbingMode() {
        stubbingMode.set(true);
    }

    public void exitStubbingMode() {
        stubbingMode.set(false);
    }

    public MockInvocation<?> getCurrent() {
        return currentInvocation;
    }

    public Object getPreviousRecursiveStub() {
        return previousRecursiveStub;
    }
}
