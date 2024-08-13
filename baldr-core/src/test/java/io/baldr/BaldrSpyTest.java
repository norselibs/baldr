package io.baldr;


import org.junit.Assert;
import org.junit.Test;

import static io.baldr.Baldr.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaldrSpyTest {


    @Test
    public void simpleVerification() {
        Car car = spy(new Car());
        car.openDoor();
        assertCalled(car, Car::openDoor);
    }

    @Test
    public void simpleNegativeVerification() {
        Car car = spy(new Car());

        try {
            assertCalled(car, Car::openDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void namedMocks() {
        Car car = spy(new Car(), "car");
        Car car2 = spy(new Car(), "car2");
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor);
            assertCalled(car2, Car::openDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of `car2`.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void inOrderVerification() {
        Car car = spy(new Car());
        car.openDoor();
        car.closeDoor();
        assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
    }

    @Test
    public void invalidInOrderVerification() {
        Car car = spy(new Car());
        car.closeDoor();
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("Car.openDoor() was expected to be called before Car.closeDoor()", e.getMessage());
        }
    }


    @Test
    public void inOrderVerification_onDifferentMocks() {
        Car car = spy(new Car());
        Car car2 = spy(new Car());
        Car car3 = spy(new Car());
        car.openDoor();
        car2.openDoor();
        car3.openDoor();

        assertCalled(car, Car::openDoor)
                .thenCalled(car2, Car::openDoor)
                .thenCalled(car3, Car::openDoor);
    }

    @Test
    public void invalidInOrderVerificationOnDifferentMocks() {
        Car car = spy(new Car());
        Car car2 = spy(new Car());
        car2.openDoor();
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(car2, Car::openDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("Car.openDoor() was expected to be called before Car.openDoor()", e.getMessage());
        }
    }

    @Test
    public void individualAssertionsDoesNotImplyOrdering() {
        Car car = spy(new Car());
        Car car2 = spy(new Car());
        car2.openDoor();
        car.openDoor();

        assertCalled(car, Car::openDoor);
        assertCalled(car2, Car::openDoor);
    }

    @Test
    public void inOrderMissingFirstVerification() {
        Car car = spy(new Car());
        car.closeDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.openDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void inOrderMissingSecondVerification() {
        Car car = spy(new Car());
        car.openDoor();

        try {
            assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.closeDoor() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void simpleParameterMatching() {
        Car car = spy(new Car());
        car.setCarName("Toyota");

        assertCalled(car, c -> c.setCarName("Toyota"));
    }


    @Test
    public void simpleFailedParameterMatching() {
        Car car = spy(new Car());
        car.setCarName("Toyota");

        try {
            assertCalled(car, c -> c.setCarName("Hyundai"));
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setCarName(\"Hyundai\") invoked on mock", e.getMessage());
        }
    }

    @Test
    public void simpleStubbing() {
        Car car = spy(new Car());
        when(car, Car::getCarName).thenReturn("Toyota");

        Assert.assertEquals("Toyota", car.getCarName());
    }

    @Test
    public void recursiveStubbing() {
        Car car = spy(new Car());
        car.setEngine(new Engine());
        when(car, c -> c.getEngine().getCylinderCount()).thenReturn(5);


        System.out.println(car.getEngine().getClass().getName());
        Assert.assertEquals(5, car.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveAutoMocking() {
        Car car = spy(new Car());
        car.setEngine(new Engine());

        Assert.assertEquals(-1, car.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveAssertion() {
        Car car = spy(new Car());
        car.setEngine(new Engine());

        car.getEngine().getCylinderCount();

        assertCalled(car, c-> c.getEngine().getCylinderCount());
    }

    @Test
    public void recursiveInvalidAssertion() {
        Car car = spy(new Car());
        car.setEngine(new Engine());
        car.getEngine();

        try {
            assertCalled(car, c-> c.getEngine().getCylinderCount());
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Engine.getCylinderCount() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void recursiveInvalidInitialCallAssertion() {
    Car car = spy(new Car());

    try {
        assertCalled(car, c-> c.getEngine().getCylinderCount());
        Assert.fail();
    } catch (MockVerificationException e) {
        assertEquals("No matching invocations of Car.getEngine() invoked on mock", e.getMessage());
    }

}

    @Test
    public void classWithConstructor() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.callService();;

        assertCalled(obj, ClassWithConstructor::callService);
        assertCalled(service, MyService::serve);
    }

    @Test
    public void recursiveVerificationOfSpy() {
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(new MyServiceImpl()));
        obj.getService().serve();

        assertCalled(obj, c -> c.getService().serve());
    }

    @Test
    public void failingRecursiveVerificationOfSpy() {
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(new MyServiceImpl()));
        obj.getService();

        try {
            assertCalled(obj, c -> c.getService().serve());
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of MyServiceImpl.serve() invoked on mock", e.getMessage());
        }
    }


    @Test
    public void recursiveVerificationOfMockedDependency() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.getService().serve();

        assertCalled(obj, c -> c.getService().serve());
    }

    @Test
    public void nonRecursiveVerificationOfMockOnSpiedInstance() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.callService();

        assertCalled(service, MyService::serve);
    }

    @Test
    public void failingRecursiveVerificationOfMockedDependency() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.getService();

        try {
            assertCalled(obj, c -> c.getService().serve());
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Object.serve() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void classWithSillyConstructor() {
        ClassWithSillyConstrcutor obj = mock(ClassWithSillyConstrcutor.class);

        assertEquals(0,obj.getI());
        assertEquals(null, obj.getIntegerBoxed());
        assertEquals(0,obj.getS());
        assertEquals(null, obj.getShortBoxed());
        assertEquals(0.0,obj.getD(), 0.1);
        assertEquals(null, obj.getDoubleBoxed());
        assertEquals(0.0f,obj.getF(), 0.1);
        assertEquals(null, obj.getFloatBoxed());
    }
}