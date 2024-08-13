package io.baldr;

import java.util.function.Supplier;

public class TestClass {

	public Supplier sup() {
		return () -> new Object();
	}
}
