package io.baldr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.baldr.Baldr.assertCalled;

@ExtendWith(BaldrExtension.class)
public class AnnotationsTestJunit {
	@Mock
	private Boat boat;

	@Test
	public void mockAnnotation() {
		boat.start();

		assertCalled(boat, Boat::start);
	}
}
