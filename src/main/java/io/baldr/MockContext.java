package io.baldr;

import org.hamcrest.Matcher;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MockContext {
    private static InheritableThreadLocal<MockContext> context =  new InheritableThreadLocal<>();
    static {
        context.set(new MockContext());
    }


    private InvocationModeEnum invocationMode = InvocationModeEnum.Invoke;
    private ConcurrentLinkedDeque<MockInvocation<?>> invocations = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<RegisteredMatcher> matchers = new ConcurrentLinkedDeque<>();
    private MockVerificationImpl currentVerification;

    public static MockContext get() {
        return context.get();
    }

    public <T> void addInvocation(MockInvocation<T> invocation) {
        invocations.add(invocation);
    }

    private void clear() {
        invocations.clear();
        matchers.clear();
        currentVerification = null;
    }

    public void enterAssert() {
        clear();
        invocationMode = InvocationModeEnum.Assert;
    }

    public void enterStubbing() {
        clear();
        invocationMode = InvocationModeEnum.Stubbing;
    }

    public void exitAssert() {
        clear();
        invocationMode = InvocationModeEnum.Invoke;
    }

    public void exitStubbing() {
        clear();
        invocationMode = InvocationModeEnum.Invoke;
    }

    public InvocationMode getInvocationMode(MockShadow mockShadow) {
        return invocationMode.get(mockShadow);
    }

    public Optional<MockInvocation> getPrevious() {
        return Optional.ofNullable(invocations.peekLast());
    }

    public boolean isNotÍnvoking() {
        return invocationMode != InvocationModeEnum.Invoke;
    }

    public <T> void registerMatcher(String id, Class parameterType, Matcher t) {
        matchers.add(new RegisteredMatcher(id, parameterType, t));
    }

    public long numberOfPrimitiveMatchers(Class type) {
        return matchers.stream().filter(m -> m.type.equals(type)).count();
    }

    public Optional<Matcher> popPrimitiveMatcher(Class type) {
        Optional<Matcher> matcher = matchers.stream().filter(m -> m.type.equals(type)).findFirst().map(m -> m.matcher);
        if (matcher.isPresent()) {
            matchers.removeIf(rm -> matcher.get().equals(rm.matcher));
            return matcher;
        }
        return Optional.empty();
    }

    public boolean hasMatcher(Class<?> aClass, String id) {
        return matchers.stream().anyMatch(m -> m.type.equals(aClass) && m.id.equals(id));
    }

    public Optional<Matcher> getPrimitiveMatcher(String id, Class type) {
        return matchers.stream().filter(m -> m.type.equals(type) && m.id.equals(id)).map(rm -> rm.matcher).findFirst();
    }

    public void setCurrentVerificationImpl(MockVerificationImpl currentVerification) {
        this.currentVerification = currentVerification;
    }

    public MockVerificationImpl getCurrentVerification() {
        return currentVerification;
    }

    private class RegisteredMatcher {
        private String id;
        private final Class type;
        private final Matcher matcher;

        public <T> RegisteredMatcher(String id, Class type, Matcher matcher) {
            this.id = id;
            this.type = type;
            this.matcher = matcher;
        }
    }
}
