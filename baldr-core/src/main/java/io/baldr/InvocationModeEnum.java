package io.baldr;

public enum InvocationModeEnum {
    Invoke, Stubbing, Assert;

    public InvocationMode get(MockShadow mockShadow) {
        switch (this) {
            case Stubbing: return new StubSetup(mockShadow);
            case Invoke: return new Invoke(mockShadow);
            case Assert: return new VerificationSetup(mockShadow);
        }
        throw new RuntimeException();
    }
}
