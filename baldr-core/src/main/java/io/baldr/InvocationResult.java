package io.baldr;

public class InvocationResult<T> {
	private T value = null;
	private boolean found = false;

	public InvocationResult(T value, boolean found) {
		this.value = value;
		this.found = found;
	}

	public static <T2> InvocationResult<T2> of(T2 o) {
		return new InvocationResult<T2>(o, true);
	}

	public static <T2> InvocationResult<T2> empty() {
		return new InvocationResult<T2>(null, false);
	}

	public T get() {
		return value;
	}

	public boolean isPresent() {
		return found;
	}
}
