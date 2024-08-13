package io.baldr;

public class ClassWithConstructor {
	private final MyService service;

	public ClassWithConstructor(MyService service) {
		this.service = service;
	}

	public MyService getService() {
		return service;
	}

	public void callService() {
		service.serve();
	}
}
