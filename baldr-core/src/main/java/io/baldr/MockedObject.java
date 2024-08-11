package io.baldr;

public interface MockedObject<T> {
    MockShadow $getShadow();
    void $setName(String name);
    String $getName();
}

