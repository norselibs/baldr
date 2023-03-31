package io.baldr;

public interface InvocationMode {
    <T> void build(MockInvocation<T> invocation);

    Object finish();
}
