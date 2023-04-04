package io.baldr;

import io.ran.Clazz;
import io.ran.Primitives;
import org.hamcrest.Matcher;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.baldr.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings("rawtypes")
public class MockInvocation<T> {
    private final MockShadow mockShadow;
    private final T on;
    private final String methodName;
    private final List<MockInvocationParameter> parameters = new ArrayList<>();
    private int order;
    private final ConcurrentLinkedQueue<Object> returnValues = new ConcurrentLinkedQueue<>();

    public MockInvocation(MockShadow mockShadow, T on, String methodName) {
        this.mockShadow = mockShadow;
        this.on = on;
        this.methodName = methodName;
    }

    public MockInvocation<T> addParameter(Class<?> pClass, String name, Object value) {
        parameters.add(new MockInvocationParameter(pClass, name, value));
        return this;
    }

    public MockInvocation<T> addParameter(String pClass, String name, Object value) {
        parameters.add(new MockInvocationParameter(Primitives.get(pClass).getPrimitive(), name, value));
        return this;
    }

    public Object end() {
        return mockShadow.finish(this);
    }

    public Optional<MockInvocation<?>> matchesAny(Queue<MockInvocation<?>> invocations) {
        return invocations.stream().filter(invocation -> invocation.matches((MockInvocation) this)).findFirst();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MockInvocation)) {
            return false;
        }
        return matches((MockInvocation<T>) obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(on, methodName, parameters);
    }

    public boolean matches(MockInvocation<T> tMockInvocation) {
        if (!(on == tMockInvocation.on)) {
            return false;
        }
        if (!(methodName.equals(tMockInvocation.methodName))) {
            return false;
        }

        for(int i=0;i<parameters.size();i++) {

            MockInvocationParameter actualParameter = parameters.get(i);
            MockInvocationParameter matcherParameter = tMockInvocation.parameters.get(i);
            /*

            */
            Matcher<?> matcher;
            if(matcherParameter.getValue() instanceof Matcher<?>) { // This requires a bit of investigation into hamcrest
                matcher = (Matcher<?>) matcherParameter.getValue();
            } else {
                Clazz matcherParameterClazz = Clazz.of(matcherParameter.getType());
                if (matcherParameter.getType().isAssignableFrom(String.class) || matcherParameterClazz.isPrimitive() || matcherParameterClazz.isBoxedPrimitive()) {
                    Optional<Matcher> specifiedMatcher = getPrimitiveMatcher(matcherParameterClazz.getBoxed().clazz, String.valueOf(matcherParameter.getValue()));
                    if(specifiedMatcher.isEmpty()) {
                        if (actualParameter.getType().isPrimitive()) {
                            long numberOfPrimitivesInParameters = parameters.stream()
                                    .filter(p -> actualParameter.getType().equals(p.getType()) && p.getType().isPrimitive())
                                    .count();
                            long numberOfPrimitiveMatchersInParameters = MockContext.get().numberOfPrimitiveMatchers(actualParameter.getType());
                            if(MockContext.get().isNotÍnvoking()
                                    && (
                                    (numberOfPrimitivesInParameters > 0 && numberOfPrimitiveMatchersInParameters > 0)
                                            && numberOfPrimitivesInParameters != numberOfPrimitiveMatchersInParameters
                            )
                            ) {
                                throw new RuntimeException("If multiple primitives as passed into a method, either all or none of the parameters must be a matcher");
                            }
                        }
                        specifiedMatcher = MockContext.get().popPrimitiveMatcher(matcherParameter.getType());
                    }
                    matcher = specifiedMatcher.isPresent() ? specifiedMatcher.get() : equalTo(matcherParameter.getValue());
                } else {
                    matcher = equalTo(matcherParameter.getValue());
                }
            }
            if (!matcher.matches(actualParameter.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return on.getClass().getSuperclass().getSimpleName()+"."+methodName+"()";
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public <R> void addReturnValue(R returnValue) {
        returnValues.add(returnValue);
    }

    public Object popReturnValue() {
        if (returnValues.size() == 1) {
            return returnValues.peek();
        } else if (!returnValues.isEmpty()) {
            return returnValues.poll();
        }
        return getMockShadow().getReturnMock(this);
    }

    public Method getMethod() {
        try {
            return on.getClass().getMethod(methodName, parameters.stream().map(MockInvocationParameter::getType).toArray(Class[]::new));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public MockShadow getMockShadow() {
        return mockShadow;
    }
}
