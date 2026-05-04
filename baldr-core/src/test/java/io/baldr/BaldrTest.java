package io.baldr;


import org.junit.Assert;
import org.junit.Test;

import static io.baldr.Baldr.*;
import static org.junit.Assert.*;

public class BaldrTest {


    @Test
    public void simpleVerification() {
        Car car = mock(Car.class);
        car.openDoor();
        on(car).assertCalled(Car::openDoor);
    }

    @Test
    public void simpleNegativeVerification() {
        Car car = mock(Car.class);

        try {
            on(car).assertCalled(Car::openDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void namedMocks() {
        Car car = mock(Car.class, "car");
        Car car2 = mock(Car.class, "car2");
        car.openDoor();

        try {
            on(car).assertCalled(Car::openDoor);
            on(car2).assertCalled(Car::openDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of `car2`.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void inOrderVerification() {
        Car car = mock(Car.class);
        car.openDoor();
        car.closeDoor();
        on(car).assertCalled(Car::openDoor).thenCalled(Car::closeDoor);
    }

    @Test
    public void invalidInOrderVerification() {
        Car car = mock(Car.class);
        car.closeDoor();
        car.openDoor();

        try {
            on(car).assertCalled(Car::openDoor).thenCalled(Car::closeDoor);
            Assert.fail();
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

        on(car).assertCalled(Car::openDoor)
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
            on(car).assertCalled(Car::openDoor).thenCalled(car2, Car::openDoor);
            Assert.fail();
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

        on(car).assertCalled(Car::openDoor);
        on(car2).assertCalled(Car::openDoor);
    }

    @Test
    public void inOrderMissingFirstVerification() {
        Car car = mock(Car.class);
        car.closeDoor();

        try {
            on(car).assertCalled(Car::openDoor).thenCalled(Car::closeDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void inOrderMissingSecondVerification() {
        Car car = mock(Car.class);
        car.openDoor();

        try {
            on(car).assertCalled(Car::openDoor).thenCalled(Car::closeDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.closeDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void simpleParameterMatching() {
        Car car = mock(Car.class);
        car.setCarName("Toyota");

        on(car).assertCalled(c -> c.setCarName("Toyota"));
    }


    @Test
    public void simpleFailedParameterMatching() {
        Car car = mock(Car.class);
        car.setCarName("Toyota");

        try {
            on(car).assertCalled(c -> c.setCarName("Hyundai"));
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setCarName(\"Hyundai\") invoked on mock", e.getMessage());
        }
    }

    @Test
    public void simpleStubbing() {
        Car car = mock(Car.class);
        on(car).when(c -> c.getCarName()).thenReturn("Toyota");

        Assert.assertEquals("Toyota", car.getCarName());
    }

    @Test
    public void recursiveStubbing() {
        Car car = mock(Car.class);
        on(car).when(c -> c.getEngine()).then(e -> e.getCylinderCount()).thenReturn(5);

        Assert.assertEquals(5, car.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveAutoMockingIntegerReturn() {
        Car car = mock(Car.class);
        car.start();

        Assert.assertEquals(0, car.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveAssertion() {
        Car car = mock(Car.class);

        car.getEngine().getCylinderCount();

        on(car).when(c -> c.getEngine()).assertCalled(e -> e.getCylinderCount());
    }

    @Test
    public void recursiveInvalidAssertion() {
        Car car = mock(Car.class);

        car.getEngine();

        try {
            on(car).when(c -> c.getEngine()).assertCalled(e -> e.getCylinderCount());
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Engine.getCylinderCount() invoked on mock", e.getMessage());

        }
    }

    @Test
    public void recursiveInvalidInitialCallAssertion() {
        Car car = mock(Car.class);

        try {
            on(car).when(c -> c.getEngine()).assertCalled(e -> e.getCylinderCount());
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.getEngine() invoked on mock", e.getMessage());
        }

    }

    @Test
    public void classWithConstructor() {
        ClassWithConstructor obj = mock(ClassWithConstructor.class);
        obj.callService();

        on(obj).assertCalled(c -> c.callService());
    }

    @Test
    public void classWithSillyConstructor() {
        ClassWithSillyConstructor obj = mock(ClassWithSillyConstructor.class);

        assertEquals(0,obj.getI());
        assertEquals(null, obj.getIntegerBoxed());
        assertEquals(0,obj.getS());
        assertEquals(null, obj.getShortBoxed());
        assertEquals(0.0,obj.getD(), 0.1);
        assertEquals(null, obj.getDoubleBoxed());
        assertEquals(0.0f,obj.getF(), 0.1);
        assertEquals(null, obj.getFloatBoxed());
    }

    @Test
    public void timesExact() {
        Car car = mock(Car.class);
        car.openDoor();
        car.openDoor();
        car.openDoor();
        on(car).assertCalled(Car::openDoor).times(3);
    }

    @Test
    public void timesTooFew() {
        Car car = mock(Car.class);
        car.openDoor();
        try {
            on(car).assertCalled(Car::openDoor).times(3);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertTrue(e.getMessage().contains("expected to be called 3 times but was only called"));
        }
    }

    @Test
    public void timesTooMany() {
        Car car = mock(Car.class);
        car.openDoor();
        car.openDoor();
        try {
            on(car).assertCalled(Car::openDoor).times(1);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertTrue(e.getMessage().contains("expected to be called exactly 1 times but was called more"));
        }
    }

    @Test
    public void assertNeverCalledPasses() {
        Car car = mock(Car.class);
        on(car).assertNeverCalled(Car::openDoor);
    }

    @Test
    public void assertNeverCalledFails() {
        Car car = mock(Car.class);
        car.openDoor();
        try {
            on(car).assertNeverCalled(Car::openDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertTrue(e.getMessage().contains("was not expected to be called but was"));
        }
    }

    @Test
    public void interfaceMocking() {
        MyService svc = mock(MyService.class);
        svc.serve();
        on(svc).assertCalled(MyService::serve);
    }

    @Test
    public void interfaceMockingNegative() {
        MyService svc = mock(MyService.class);
        try {
            on(svc).assertCalled(MyService::serve);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertTrue(e.getMessage().contains("No matching invocations"));
        }
    }

    @Test
    public void thenThrowOnReturnMethod() {
        Car car = mock(Car.class);
        on(car).when(c -> c.getCarName()).thenThrow(new IllegalStateException("boom"));
        try {
            car.getCarName();
            Assert.fail();
        } catch (IllegalStateException e) {
            assertEquals("boom", e.getMessage());
        }
    }

    @Test
    public void thenThrowOnVoidMethod() {
        Car car = mock(Car.class);
        when(car, Car::openDoor).thenThrow(new IllegalStateException("door stuck"));
        try {
            car.openDoor();
            Assert.fail();
        } catch (IllegalStateException e) {
            assertEquals("door stuck", e.getMessage());
        }
    }

    @Test
    public void thenReturnThenThrow() {
        Car car = mock(Car.class);
        on(car).when(c -> c.getCarName()).thenReturn("Toyota").thenThrow(new IllegalStateException("no more"));
        assertEquals("Toyota", car.getCarName());
        try {
            car.getCarName();
            Assert.fail();
        } catch (IllegalStateException e) {
            assertEquals("no more", e.getMessage());
        }
    }

    @Test
    public void whenOnNonMockThrows() {
        Car car = new Car();
        try {
            on(car).when(c -> c.getCarName());
            Assert.fail();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("must be a mock"));
        }
    }
}
