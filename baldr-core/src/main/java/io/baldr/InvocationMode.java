package io.baldr;

import java.util.Optional;

public interface InvocationMode {
    <T> void build(MockInvocation<T> invocation);

    InvocationResult<Object> finish(MockInvocation<?> invocation);
}
