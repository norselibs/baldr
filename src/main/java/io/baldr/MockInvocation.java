package io.baldr;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.Matchers.equalTo;

public class MockInvocation<T> {
    private MockInvocations mockInvocations;
    private final T on;
    private final String methodName;
    private final List<MockInvocationParameter> parameters = new ArrayList<>();

    public MockInvocation(MockInvocations mockInvocations, T on, String methodName) {
        this.mockInvocations = mockInvocations;
        this.on = on;
        this.methodName = methodName;
    }

    public MockInvocation<T> addParameter(Class<?> pClass, String name, Object value) {
        parameters.add(new MockInvocationParameter(pClass, name, value));
        return this;
    }

    public void end() {
        mockInvocations.finish();
    }

    public boolean matchesAny(Queue<MockInvocation<?>> invocations) {
        return invocations.stream().anyMatch(invocation -> {
            return invocation.matches((MockInvocation) this);
        });
    }

    private boolean matches(MockInvocation<T> tMockInvocation) {
        if (!(on == tMockInvocation.on)) {
            return false;
        }
        if (!(methodName.equals(tMockInvocation.methodName))) {
            return false;
        }
        for(int i=0;i<parameters.size();i++) {
            MockInvocationParameter matcherParameter = parameters.get(i);
            MockInvocationParameter actualParameter = tMockInvocation.parameters.get(i);
            Matcher<?> matcher;
            if(matcherParameter instanceof Matcher<?>) {
                matcher = (Matcher<?>) matcherParameter;
            } else {
                matcher = equalTo(matcherParameter);
            }
            if (!matcher.matches(actualParameter)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return on.getClass().getSuperclass().getSimpleName()+"."+methodName+"()";
    }
}
