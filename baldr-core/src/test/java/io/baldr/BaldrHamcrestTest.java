package io.baldr;


import org.junit.Assert;
import org.junit.Test;

import static io.baldr.Baldr.*;
import static io.baldr.Matchers.*;
import static org.junit.Assert.assertEquals;

public class BaldrHamcrestTest {


    @Test
    public void hamcrestMatchingVerification_equalTo() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setEngine(engine);
        assertCalled(car, c -> c.setEngine(equalTo(engine)));
    }

    @Test
    public void hamcrestMatchingVerification_equalTo_failCase() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setEngine(new Engine());

        try {
            assertCalled(car, c -> c.setEngine(equalTo(engine)));
        } catch (MockVerificationException e) {
            Assert.assertTrue(e.getMessage() + " did not start with expected value", e.getMessage().startsWith("No matching invocations of `Car`.setEngine(<io.baldr.Engine@"));
        }
    }

    @Test
    public void hamcrestMatchingVerification_not_equalTo() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setEngine(new Engine());

        assertCalled(car, c -> c.setEngine(not(equalTo(engine))));
    }

    @Test
    public void hamcrestMatchingVerification_not_equalTo_failCase() {
        Engine engine = new Engine();
        Car car = mock(Car.class);
        car.setEngine(engine);

        try {
            assertCalled(car, c -> c.setEngine(not(equalTo(engine))));
        } catch (MockVerificationException e) {
            Assert.assertTrue(e.getMessage()+" did not start with expected value",e.getMessage().startsWith("No matching invocations of Car.setEngine(<io.baldr.Engine@"));
        }
    }

    @Test
    public void hamcrestMatchingVerification_nullValue() {
        Car car = mock(Car.class);
        car.setEngine(null);

        assertCalled(car, c -> c.setEngine(nullValue(Engine.class)));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_nullValue_failCase() {
        Car car = mock(Car.class);
        car.setEngine(new Engine());

        try {
            assertCalled(car, c -> c.setEngine(nullValue(Engine.class)));
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setEngine(null) invoked on mock", e.getMessage());
        }
    }

    @Test
    public void hamcrestMatchingVerification_notNullValue() {
        Car car = mock(Car.class);
        car.setEngine(new Engine());

        assertCalled(car, c -> c.setEngine(notNullValue(Engine.class)));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_notNullValue_failCase() {
        Car car = mock(Car.class);
        car.setEngine(null);

        try {
            assertCalled(car, c -> c.setEngine(notNullValue(Engine.class)));
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setEngine(not null) invoked on mock", e.getMessage());
        }
    }

    @Test
    public void hamcrestMatchingVerification_any() {
        Car car = mock(Car.class);
        car.setEngine(new Engine());

        assertCalled(car, c -> c.setEngine(any(Engine.class)));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_any_nullValue() {
        Car car = mock(Car.class);
        car.setEngine(null);

        try {
            assertCalled(car, c -> c.setEngine(any(Engine.class)));
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setEngine(an instance of io.baldr.Engine) invoked on mock", e.getMessage());
        }
    }

    @Test
    public void hamcrestMatchingVerification_anything() {
        Car car = mock(Car.class);
        car.setEngine(new Engine());

        assertCalled(car, c -> c.setEngine(anything(Engine.class)));
    }

    @Test
    public void hamcrestMatchingVerification_anything_nullValue() {
        Car car = mock(Car.class);
        car.setEngine(null);

        assertCalled(car, c -> c.setEngine(anything(Engine.class)));
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
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(equalTo("Hyundai")));
    }

    @Test
    public void hamcrestMatchingVerification_startsWith() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(startsWith("Hyun")));
    }

    @Test
    public void hamcrestMatchingVerification_startsWithIgnoringCase() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(startsWithIgnoringCase("hyun")));
    }

    @Test
    public void hamcrestMatchingVerification_endsWith() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(endsWith("dai")));
    }

    @Test
    public void hamcrestMatchingVerification_endsWithIgnoringCase() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(endsWithIgnoringCase("daI")));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_endsWithIgnoringCase_failCase() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        try {
            assertCalled(car, c -> c.setCarName(endsWithIgnoringCase("Hyun")));
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setCarName(a string ending with \"Hyun\" ignoring case) invoked on mock", e.getMessage());
        }
    }

    @Test
    public void hamcrestMatchingVerification_matchesPatternString() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        assertCalled(car, c -> c.setCarName(matchesPattern(".+da.+")));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_matchesPatternString_failCase() {
        Car car = mock(Car.class);
        car.setCarName("Hyundai");
        try {
            assertCalled(car, c -> c.setCarName(matchesPattern(".+ad.+")));
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Car.setCarName(a string matching the pattern '.+ad.+') invoked on mock", e.getMessage());
        }
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnInteger() {
        Engine engine = mock(Engine.class);
        engine.setCylinderCount(5);
        assertCalled(engine, e -> e.setCylinderCount(equalTo(5)));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_equalToOnInteger_failCase() {
        Engine engine = mock(Engine.class);
        engine.setCylinderCount(3);
        try {
            assertCalled(engine, e -> e.setCylinderCount(equalTo(5)));
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Engine.setCylinderCount(<5>) invoked on mock", e.getMessage());
        }
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

    @Test(expected = AssertionError.class)
    public void failing_hamcrestMatchingVerification_equalToOnMultipleBooleans() {
        Engine engine = mock(Engine.class);
        engine.setStarted(true);
        engine.setEnabled(false);
        assertCalled(engine, e -> e.setStarted(equalTo(true)));
        assertCalled(engine, e -> e.setStarted(equalTo(false)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnMultipleBooleansInSameMethod_withoutMatchers() {
        Engine engine = mock(Engine.class);
        engine.multipleBooleans(true, false, true);
        assertCalled(engine, e -> e.multipleBooleans(true, false, true));
    }

    @Test(expected = AssertionError.class)
    public void hamcrestMatchingVerification_equalToOnMultipleBooleansInSameMethod_withoutMatchers_failing() {
        Engine engine = mock(Engine.class);
        engine.multipleBooleans(false, true, false);
        try {
            assertCalled(engine, e -> e.multipleBooleans(true, false, true));
            Assert.fail();
        } catch (Exception e) {
            assertEquals("No matching invocations of Engine.multipleBooleans(<true>, <false>, <true>) invoked on mock", e.getMessage());
        }
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnMultipleBooleansInSameMethod_withMatchers() {
        Engine engine = mock(Engine.class);
        engine.multipleBooleans(true, false, true);
        assertCalled(engine, e -> e.multipleBooleans(equalTo(true), equalTo(false), equalTo(true)));
    }

    @Test
    public void hamcrestMatchingVerification_equalToOnMultipleBooleansInSameMethod_withSomeMatchers() {
        Engine engine = mock(Engine.class);
        engine.multipleBooleans(true, false, true);
        try {
            assertCalled(engine, e -> e.multipleBooleans(equalTo(true), false, equalTo(true)));
            Assert.fail();
        } catch (MultiplePrimitivesWithMixedMatchersException e) {
            assertEquals("If multiple primitives as passed into a method, either all or none of the parameters must be a matcher", e.getMessage());
        }
    }

    @Test
    public void changingMatcherOrNoMatchersBetweenDifferentMethods() {
        Engine engine = mock(Engine.class);
        engine.setCylinderCount(4);
        engine.setCylinderCount(8);
        engine.setCylinderCount(6);

        assertCalled(engine, e -> e.setCylinderCount(equalTo(4)))
                .thenCalled(e -> e.setCylinderCount(8))
                .thenCalled(e -> e.setCylinderCount(equalTo(6)));
    }

    @Test
    public void delegateMatcher() {
        Engine engine = mock(Engine.class);
        engine.setStarted(false);

         assertCalled(engine, e -> e.setStarted(delegate(Boolean.class,b -> !b)));
    }

    @Test(expected = AssertionError.class)
    public void delegateMatcher_fails() {
        Engine engine = mock(Engine.class);
        engine.setStarted(true);

        try {
            assertCalled(engine, e -> e.setStarted(delegate(Boolean.class, b -> b)));
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Engine.setStarted(<true>) invoked on mock", e.getMessage());
        }
    }

    @Test(expected = AssertionError.class)
    public void delegateMatcher_missingInvocation() {
        Engine engine = mock(Engine.class);

        try {
            assertCalled(engine, e -> e.setStarted(delegate(Boolean.class, b -> false)));
            Assert.fail();
        } catch (MockVerificationException e) {
            assertEquals("No matching invocations of Engine.setStarted(<delegate matcher>) invoked on mock", e.getMessage());
        }
    }
}