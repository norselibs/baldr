package io.baldr;


import org.junit.Test;

import static io.baldr.Baldr.*;
import static io.baldr.hamcrest.Matchers.equalTo;
import static io.baldr.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaldrHamcrestTest {


    @Test
    public void hamcrestMatchingVerification_equalTo() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setEngine(engine);
        assertCalled(car, c -> c.setEngine(equalTo(engine)));
    }

    @Test
    public void hamcrestMatchingVerification_same() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setEngine(engine);
        assertCalled(car, c -> c.setEngine(sameInstance(engine)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnString() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(equalTo("Hyundai")));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnInteger() {
        Engine engine = mock(Engine.class);
        engine.setCylinderCount(5);
        assertCalled(engine, e -> e.setCylinderCount(equalTo(5)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnLong() {
        Engine engine = mock(Engine.class);
        engine.setSerialNumber(587687686L);
        assertCalled(engine, e -> e.setSerialNumber(equalTo(587687686L)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnShort() {
        Engine engine = mock(Engine.class);
        engine.setType((short) 66);
        assertCalled(engine, e -> e.setType(equalTo((short)66)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnChar() {
        Engine engine = mock(Engine.class);
        engine.setTypeLetter((char) 66);
        assertCalled(engine, e -> e.setTypeLetter(equalTo((char)66)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnFloat() {
        Engine engine = mock(Engine.class);
        engine.setDisplacement(66f);
        assertCalled(engine, e -> e.setDisplacement(equalTo(66f)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnDouble() {
        Engine engine = mock(Engine.class);
        engine.setLength(66.0);
        assertCalled(engine, e -> e.setLength(equalTo(66.0)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnByte() {
        Engine engine = mock(Engine.class);
        engine.setShortCode((byte) 5);
        assertCalled(engine, e -> e.setShortCode(equalTo((byte)5)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnBoolean() {
        Engine engine = mock(Engine.class);
        engine.setStarted(true);
        assertCalled(engine, e -> e.setStarted(equalTo(true)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnMultipleBooleans() {
        Engine engine = mock(Engine.class);
        engine.setStarted(true);
        engine.setEnabled(false);
        assertCalled(engine, e -> e.setStarted(equalTo(true)))
                .thenCalled(e -> e.setEnabled(equalTo(false)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnMultipleBooleans_sameMethodMultipleTimes() {
        Engine engine = mock(Engine.class);
        engine.setStarted(true);
        engine.setStarted(false);
        assertCalled(engine, e -> e.setStarted(equalTo(true)))
                .thenCalled(e -> e.setStarted(equalTo(false)));
    }

    @Test
    public void failing_hamcrestMatchingVerification_equalToOnMultipleBooleans() {
        Engine engine = mock(Engine.class);
        engine.setStarted(true);
        engine.setEnabled(false);
        assertCalled(engine, e -> e.setStarted(equalTo(true)));
        assertCalled(engine, e -> e.setEnabled(equalTo(false)));
    }
}