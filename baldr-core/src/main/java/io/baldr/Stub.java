package io.baldr;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"rawtypes","unchecked"})
public class Stub<T, R> {
    private final MockInvocation<?> stubInvocation;

    public Stub(T on, Function<T, R> consumer) {
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }
        consumer.apply(on);
        stubInvocation = MockContext.get().getPrevious().orElseThrow();
    }

    public Stub(T on, Consumer<T> consumer) {
        if (!(on instanceof MockedObject)) {
            throw new RuntimeException(on.getClass().getName()+" must be a mock");
        }
        consumer.accept(on);
        stubInvocation = MockContext.get().getPrevious().orElseThrow();
    }

    public Stub<T, R> thenReturn(R returnValue) {
        stubInvocation.addReturnValue(returnValue);
        return this;
    }

    public Stub<T, R> thenThrow(Throwable throwable) {
        stubInvocation.addThrowable(throwable);
        return this;
    }

    public <N> Stub<R, N> then(Function<R, N> function) {
        MockedObject<R> returnMock = (MockedObject<R>) stubInvocation.getMockShadow().getReturnMock(stubInvocation);
        MockContext.get().enterStubbing();
        try {
            return new Stub<R, N>((R) returnMock, function);
        } finally {
            MockContext.get().exitStubbing();
        }
    }

    public Stub<R, Void> then(Consumer<R> consumer) {
        MockedObject<R> returnMock = (MockedObject<R>) stubInvocation.getMockShadow().getReturnMock(stubInvocation);
        MockContext.get().enterStubbing();
        try {
            return new Stub<R, Void>((R) returnMock, consumer);
        } finally {
            MockContext.get().exitStubbing();
        }
    }
}
