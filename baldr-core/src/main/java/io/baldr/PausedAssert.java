package io.baldr;

public class PausedAssert implements InvocationMode {
	private final MockShadow mockShadow;

	public PausedAssert(MockShadow mockShadow) {
		this.mockShadow = mockShadow;
	}

	@Override
	public <T> void build(MockInvocation<T> invocation) {

	}

	@Override
	public InvocationResult<Object> finish(MockInvocation<?> invocation) {
		return InvocationResult.empty();
	}
}
