package io.baldr;

import java.util.function.Function;

public class Stub<T, R> {
    private final MockInvocation<?> stubInvocation;

    public Stub(T on, Function<T, R> consumer) {
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }

        MockInvocations mockInvocations = ((MockedObject<?>) on).$getInvocations();
        mockInvocations.enterStubbingMode();
        consumer.apply(on);

        Object previousRecursiveStub = mockInvocations.getPreviousRecursiveStub();
        if (previousRecursiveStub == null || previousRecursiveStub == on) {
            stubInvocation = mockInvocations.getCurrent();
        } else {
            MockInvocations otherInvocations = ((MockedObject<?>) previousRecursiveStub).$getInvocations();
            otherInvocations.exitStubbingMode();
            stubInvocation = otherInvocations.getCurrent();
        }

        mockInvocations.exitStubbingMode();
    }

    public void thenReturn(R returnValue) {
        stubInvocation.addReturnValue(returnValue);
    }
}
