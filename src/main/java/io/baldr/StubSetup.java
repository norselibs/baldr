package io.baldr;

import io.ran.Clazz;

import java.util.Collections;

import static io.baldr.Baldr.mock;

public class StubSetup implements InvocationMode {
    private MockShadow mockShadow;

    public StubSetup(MockShadow mockShadow) {
        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {

    }

    @Override
    public Object finish() {
        mockShadow.addStub(mockShadow.getCurrent());
        Clazz<?> returnType = Clazz.of(mockShadow.getCurrent().getMethod().getReturnType());
        if (returnType.isPrimitive() || returnType.isBoxedPrimitive()) {
            return returnType.getDefaultValue();
        } else if (returnType.is(Clazz.of(String.class), Collections.emptySet())) {
            return null;
        } else {
            mockShadow.setPreviousRecursiveStub(mock(returnType.clazz));
            mockShadow.exitStubbingMode();
            mockShadow.getPreviousRecursiveStub().$getInvocations().enterStubbingMode();

            mockShadow.getCurrent().addReturnValue(mockShadow.getPreviousRecursiveStub());
            return mockShadow.getPreviousRecursiveStub();
        }
    }
}
