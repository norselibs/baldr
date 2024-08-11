package io.baldr;

public class MockVerificationException extends AssertionError {
    public MockVerificationException(String s) {
        super(s);
    }
}
