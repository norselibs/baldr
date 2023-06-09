package io.baldr;

import io.ran.Clazz;

import java.util.Collections;
import java.util.Optional;

public class VerificationSetup implements InvocationMode {
    private MockShadow mockShadow;

    public VerificationSetup(MockShadow mockShadow) {

        this.mockShadow = mockShadow;
    }

    @Override
    public <T> void build(MockInvocation<T> invocation) {
        int a = 0;
    }

    @Override
    public Optional<Object> finish(MockInvocation invocation) {
        Optional<MockInvocation> match = mockShadow.popMatchingInvocation(invocation);

        if(match.isEmpty()) {
            throw new MockVerificationException("No matching invocations of "+ mockShadow.getCurrent().toString()+" invoked on mock");
        }
        Optional<MockInvocation> previous = MockContext.get().getCurrentVerification().getPrevious();
        if (previous.isPresent()) {
            int previousOrder = previous.get().getOrder();
            int currentOrder = match.get().getOrder();
            if (previousOrder > -1 && previousOrder > currentOrder) {
                throw new MockVerificationException(previous.get().toString() + " was expected to be called before " + match.get());
            }
        }

        MockContext.get().getCurrentVerification().setPrevious(match.get());
        return Optional.empty();

    }
}
