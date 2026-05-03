package io.baldr;

public enum InvocationModeEnum {
    Invoke, Stubbing, Assert, NeverAssert, Pause;

    public InvocationMode get(MockShadow mockShadow) {
        switch (this) {
            case Stubbing: return new StubSetup(mockShadow);
            case Invoke: return new Invoke(mockShadow);
            case Assert: return new VerificationSetup(mockShadow);
            case NeverAssert: return new NeverVerificationSetup(mockShadow);
            case Pause: return new PausedAssert(mockShadow);
        }
        throw new RuntimeException();
    }
}
