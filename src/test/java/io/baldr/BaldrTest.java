package io.baldr;


import org.junit.Test;

import static io.baldr.Baldr.assertCalled;
import static io.baldr.Baldr.mock;
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
        }
    }
}