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
        on(car).assertCalled(Car::openDoor);
    }

    @Test
    public void simpleNegativeVerification() {
        Car car = spy(new Car());

        try {
            on(car).assertCalled(Car::openDoor);
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
            on(car).assertCalled(Car::openDoor);
            on(car2).assertCalled(Car::openDoor);
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
        on(car).assertCalled(Car::openDoor).thenCalled(Car::closeDoor);
    }

    @Test
    public void invalidInOrderVerification() {
        Car car = spy(new Car());
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
        Car car = spy(new Car());
        Car car2 = spy(new Car());
        Car car3 = spy(new Car());
        car.openDoor();
        car2.openDoor();
        car3.openDoor();

        on(car).assertCalled(Car::openDoor)
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
            on(car).assertCalled(Car::openDoor).thenCalled(car2, Car::openDoor);
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

        on(car).assertCalled(Car::openDoor);
        on(car2).assertCalled(Car::openDoor);
    }

    @Test
    public void inOrderMissingFirstVerification() {
        Car car = spy(new Car());
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
        Car car = spy(new Car());
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
        Car car = spy(new Car());
        car.setCarName("Toyota");

        on(car).assertCalled(c -> c.setCarName("Toyota"));
    }


    @Test
    public void simpleFailedParameterMatching() {
        Car car = spy(new Car());
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
        Car car = spy(new Car());
        on(car).when(c -> c.getCarName()).thenReturn("Toyota");

        Assert.assertEquals("Toyota", car.getCarName());
    }

    @Test
    public void recursiveStubbing() {
        Car car = spy(new Car());
        car.setEngine(new Engine());
        on(car).when(c -> c.getEngine()).then(e -> e.getCylinderCount()).thenReturn(5);

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

        on(car).when(c -> c.getEngine()).assertCalled(e -> e.getCylinderCount());
    }

    @Test
    public void recursiveInvalidAssertion() {
        Car car = spy(new Car());
        car.setEngine(new Engine());
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
        Car car = spy(new Car());

        try {
            on(car).when(c -> c.getEngine()).assertCalled(e -> e.getCylinderCount());
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.getEngine() invoked on mock", e.getMessage());
        }
    }

    @Test
    public void classWithConstructor() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.callService();

        on(obj).assertCalled(ClassWithConstructor::callService);
        on(service).assertCalled(MyService::serve);
    }

    @Test
    public void recursiveVerificationOfSpy() {
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(new MyServiceImpl()));
        obj.getService().serve();

        on(obj).assertCalled(c -> c.getService().serve());
    }

    @Test
    public void failingRecursiveVerificationOfSpy() {
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(new MyServiceImpl()));
        obj.getService();

        try {
            on(obj).assertCalled(c -> c.getService().serve());
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

        on(obj).assertCalled(c -> c.getService().serve());
    }

    @Test
    public void nonRecursiveVerificationOfMockOnSpiedInstance() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.callService();

        on(service).assertCalled(MyService::serve);
    }

    @Test
    public void failingRecursiveVerificationOfMockedDependency() {
        MyService service = mock(MyService.class);
        ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(service));
        obj.getService();

        try {
            on(obj).assertCalled(c -> c.getService().serve());
            fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Object.serve() invoked on mock", e.getMessage());
        }
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
}
