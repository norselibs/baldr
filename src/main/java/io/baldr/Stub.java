package io.baldr;

import java.util.function.Function;

public class Stub<T, R> {
    private final MockInvocation<?> stubInvocation;

    public Stub(T on, Function<T, R> consumer) {
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }

        MockShadow mockShadow = ((MockedObject<?>) on).$getInvocations();
        mockShadow.enterStubbingMode();
        consumer.apply(on);

        Object previousRecursiveStub = mockShadow.getActiveStub();
        if (previousRecursiveStub == null || previousRecursiveStub == on) {
            stubInvocation = mockShadow.getCurrent();
        } else {
            MockShadow otherInvocations = ((MockedObject<?>) previousRecursiveStub).$getInvocations();
            otherInvocations.exitStubbingMode();
            stubInvocation = otherInvocations.getCurrent();
        }

        mockShadow.exitStubbingMode();
    }

    public void thenReturn(R returnValue) {
        stubInvocation.addReturnValue(returnValue);
    }
}
