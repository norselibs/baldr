package io.baldr;

public class MockInvocationParameter {
    private final Class<?> pClass;
    private final String name;
    private final Object value;

    public MockInvocationParameter(Class<?> pClass, String name, Object value) {
        this.pClass = pClass;
        this.name = name;
        this.value = value;
    }
}
