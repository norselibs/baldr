
Baldr is a simple little mock framework, originally intended as a proof of concept on how to make a mock syntax that works equally well for methods that return void as any other.

It is intended to be non opinionated, so it tries to do what you may need to do, no matter what coding principles you follow.
For one, Baldr mocks and spies are recursive in nature.

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
    // Setup
    Car car = mock(Car.class);

    // Execution
    car.openDoor();
    car.closeDoor();

	// Verification
    assertCalled(car, Car::openDoor).thenCalled(Car::closeDoor);
```

Unlike most other mock frameworks, Baldr defaults to having methods returning mockable objects return a "recursive mock" automatically:

```java
    // Setup
    Car car = mock(Car.class);
    when(car, c -> c.getEngine().getCylinderCount()).thenReturn(5);

    // Execution
    assertEquals(5, car.getEngine().getCylinderCount());

    // Verification
    assertCalled(car, c-> c.getEngine().getCylinderCount());
```

There is also, at the moment limited, support for hamcrest matchers:

```java
    // Setup
    Engine engine = new Engine();
    Car car = mock(Car.class);
    
    // Execution
    car.setEngine(engine);
    
    // Verification
    assertCalled(car, c ->c.setEngine(sameInstance(engine)));
```
Spies of real objects can also be made:

```java
    // Setup
    Car car = spy(new Car());

    // Execution
    car.openDoor();
    
    // Verification
    assertCalled(car, Car::openDoor);
```

As spies are also recursive, we can verify something being called on something which is not initially setup to be a mock

```java
    // Setup
    ClassWithConstructor obj = Baldr.spy(new ClassWithConstructor(new MyServiceImpl()));

    // Execution
    obj.getService().serve();

	// Verification
    assertCalled(obj, c -> c.getService().serve());
```

