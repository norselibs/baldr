package io.baldr;

import javax.management.monitor.MonitorSettingException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockInvocations {
    ConcurrentLinkedQueue<MockInvocation<?>> invocations = new ConcurrentLinkedQueue<>();
    private AtomicBoolean verificationMode = new AtomicBoolean(false);
    private MockInvocation<?> currentVerification;

    public static MockInvocations get() {
        return new MockInvocations();
    }

    public <T> MockInvocation<T> buildInvocation(T on,String methodName) {
        MockInvocation<T> invocation = new MockInvocation<>(this, on, methodName);

        if (!verificationMode.get()) {
            invocations.add(invocation);
        } else {
            currentVerification =  invocation;
        }
        return invocation;
    }

    public void enterVerificationMode() {
        verificationMode.set(true);
    }

    public void exitVerificationMode() {
        verificationMode.set(false);
    }

    public void finish() {
        if (verificationMode.get()) {
            if(!currentVerification.matchesAny(invocations)) {
                throw new MockVerificationException("No invocations of "+currentVerification.toString()+" invoked on mock");
            }
            currentVerification = null;
        }
    }

}
