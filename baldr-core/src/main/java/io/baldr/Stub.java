package io.baldr;

import java.util.function.Function;

public class Stub<T, R> {
    private final MockInvocation<?> stubInvocation;

    public Stub(T on, Function<T, R> consumer) {
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }

        consumer.apply(on);
        stubInvocation = MockContext.get().getPrevious().orElseThrow();

    }

    public void thenReturn(R returnValue) {
        stubInvocation.addReturnValue(returnValue);
    }
}
