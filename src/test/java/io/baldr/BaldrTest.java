package io.baldr;


import org.junit.Test;

import static io.baldr.Baldr.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaldrTest {


    @Test
    public void simpleVerification() {
        Car car = mock(Car.class);
        car.openDoor();
        assertCalled(car, Car::openDoor);
    }

    @Test
    public void simpleNegativeVerification() {
        Car car = mock(Car.class);

        try {
            assertCalled(car, Car::openDoor);
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void inOrderVerification() {
        Car car = mock(Car.class);
        car.openDoor();
        car.closeDoor();
        assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
    }

    @Test
    public void invalidInOrderVerification() {
        Car car = mock(Car.class);
        car.closeDoor();
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
            fail();
        } catch (MockVerificationException e) {
            assertEquals("Car.openDoor() was expected to be called before Car.closeDoor()", e.getMessage());
        }
    }


    @Test
    public void inOrderVerification_onDifferentMocks() {
        Car car = mock(Car.class);
        Car car2 = mock(Car.class);
        Car car3 = mock(Car.class);
        car.openDoor();
        car2.openDoor();
        car3.openDoor();

        assertCalled(car, Car::openDoor)
                .thenCalled(car2, Car::openDoor)
                .thenCalled(car3, Car::openDoor);
    }

    @Test
    public void invalidInOrderVerificationOnDifferentMocks() {
        Car car = mock(Car.class);
        Car car2 = mock(Car.class);
        car2.openDoor();
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(car2, Car::openDoor);
            fail();
        } catch (MockVerificationException e) {
            assertEquals("Car.openDoor() was expected to be called before Car.openDoor()", e.getMessage());
        }
    }

    @Test
    public void individualAssertionsDoesNotImplyOrdering() {
        Car car = mock(Car.class);
        Car car2 = mock(Car.class);
        car2.openDoor();
        car.openDoor();

        assertCalled(car, Car::openDoor);
        assertCalled(car2, Car::openDoor);
    }

    @Test
    public void inOrderMissingFirstVerification() {
        Car car = mock(Car.class);
        car.closeDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void inOrderMissingSecondVerification() {
        Car car = mock(Car.class);
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.closeDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void simpleParameterMatching() {
        Car car = mock(Car.class);
        car.setCarName("Toyota");

        assertCalled(car, c -> c.setCarName("Toyota"));
    }


    @Test
    public void simpleFailedParameterMatching() {
        Car car = mock(Car.class);
        car.setCarName("Toyota");

        try {
            assertCalled(car, c -> c.setCarName("Hyundai"));
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setCarName(\"Hyundai\") invoked on mock", e.getMessage());
        }
    }

    @Test
    public void simpleStubbing() {
        Car car = mock(Car.class);
        when(car, Car::getCarName).thenReturn("Toyota");

        assertEquals("Toyota", car.getCarName());
    }

    @Test
    public void recursiveStubbing() {
        Car car = mock(Car.class);
        when(car, c -> c.getEngine().getCylinderCount()).thenReturn(5);

        assertEquals(5, car.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveAssertion() {
        Car car = mock(Car.class);

        car.getEngine().getCylinderCount();

        assertCalled(car, c-> c.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveInvalidAssertion() {
        Car car = mock(Car.class);

        car.getEngine();

        try {
            assertCalled(car, c-> c.getEngine().getCylinderCount());
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Engine.getCylinderCount() invoked on mock", e.getMessage());

        }
    }

    @Test
    public void recursiveInvalidInitialCallAssertion() {
        Car car = mock(Car.class);

        try {
            assertCalled(car, c-> c.getEngine().getCylinderCount());
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.getEngine() invoked on mock", e.getMessage());
        }

    }
}