package io.baldr;

public class Car {
    private String name;
    private Engine engine;

    public void openDoor() {

    }

    public void closeDoor() {

    }

    public void setCarName(String name) {
        this.name = name;

    }

    public String getCarName() {
        return name;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

	public void start() {
        getEngine().setStarted(true);
	}
}
