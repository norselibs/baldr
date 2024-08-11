package io.baldr;

@SuppressWarnings("rawtypes")
public class MockInvocationParameter {
    private final MockInvocation mockInvocation;
    private final Class<?> pClass;
    private final String name;
    private final Object value;

    public MockInvocationParameter(MockInvocation mockInvocation, Class<?> pClass, String name, Object value) {
        this.mockInvocation = mockInvocation;
        this.pClass = pClass;
        this.name = name;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Class getType() {
        return pClass;
    }

    public String getName() {
        return name;
    }

    public MockInvocation getMockInvocation() {
        return mockInvocation;
    }

    @Override
    public String toString() {
        if (String.class.isAssignableFrom(pClass)) {
            return "\""+value.toString()+"\"";
        }
        return value.toString();
    }
}
