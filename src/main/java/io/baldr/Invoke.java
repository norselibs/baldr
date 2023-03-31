package io.baldr;

import io.ran.Clazz;

import java.util.Optional;

import static io.baldr.Baldr.mock;

public class Invoke implements InvocationMode {
    private MockShadow mockShadow;

    public Invoke(MockShadow mockShadow) {
        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {
        invocation.setOrder(mockShadow.incrementOrder());
        mockShadow.addInvocation(invocation);
    }

    @Override
    public Object finish() {
        Optional<MockInvocation<?>> stub = mockShadow.getStubs().stream().filter(s -> s.matches((MockInvocation)mockShadow.getCurrent())).findFirst();
        if (stub.isPresent()) {
            return stub.get().popReturnValue();
        } else {
            Clazz<?> returnType = Clazz.of(mockShadow.getCurrent().getMethod().getReturnType());
            if (returnType.isPrimitive() || returnType.isBoxedPrimitive()) {
                return returnType.getDefaultValue();
            } else {
                return mock(returnType.clazz);
            }
        }

    }
}
