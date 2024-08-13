package io.baldr;

public interface SpiedObject<T> extends MockedObject<T> {
	void $setInstance(Object obj);
	T $getInstance();
}
