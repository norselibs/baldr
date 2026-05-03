package io.baldr;

import io.ran.Clazz;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BaldrExtension implements BeforeEachCallback {
	@Override
	public void beforeEach(ExtensionContext context) {
		Object testInstance = context.getRequiredTestInstance();
		((Clazz<?>)Clazz.of(testInstance.getClass())).getFields()
				.forEach(field -> {
					try {
						field.setAccessible(true);
						if (field.getAnnotation(Mock.class) != null) {
							field.set(testInstance, Baldr.mock(field.getType()));
						} else if (field.getAnnotation(Spy.class) != null) {
							Object instance = field.get(testInstance);
							if (instance == null) {
								instance = field.getType().getConstructor().newInstance();
							}
							field.set(testInstance, Baldr.spy(instance));
						}
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException(e);
					}
				});
	}
}
