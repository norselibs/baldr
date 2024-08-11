package io.baldr;

import io.ran.Clazz;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BaldrExtension implements BeforeEachCallback {
	@Override
	public void beforeEach(ExtensionContext context) {
		((Clazz<?>)Clazz.of(context.getRequiredTestInstance().getClass())).getFields()
				.forEach(field -> {
					if (field.getAnnotation(Mock.class) != null) {
						try {
							field.setAccessible(true);
							field.set(context.getRequiredTestInstance(), Baldr.mock(field.getType()));
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
				});
	}
}
