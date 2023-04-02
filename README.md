
Baldr is a simple little mock framework, originally intended as a proof of concept on how to make a mock syntax that works equally well for methods that return void as any other.

```java
    // Setup
    Car car = mock(Car.class);

    // Execution
    car.openDoor();
    
    // Verification
    assertCalled(car, Car::openDoor);
```

Baldr also supports verifying order of invocation, like so:

```java
    Car car = mock(Car.class);
    car.openDoor();
    car.closeDoor();
    assertCalled(car, Car::openDoor).thenCalled(Car::openDoor);
```

Unlike most other mock frameworks, Baldr defaults to having methods returning mockable objects return a "recursive mock" automatically:

```java
    Car car = mock(Car.class);
    when(car, c -> c.getEngine().getCylinderCount()).thenReturn(5);

    assertEquals(5, car.getEngine().getCylinderCount());

    assertCalled(car, c-> c.getEngine().getCylinderCount());
```

There is also, at the moment limited, support for hamcrest matchers:

```java
    import static io.baldr.hamcrest.Matchers.*;

    Engine engine = new Engine();
    Car car = mock(Car.class);
    car.setEngine(engine);
    assertCalled(car, c -> c.setEngine(sameInstance(engine)));
```
