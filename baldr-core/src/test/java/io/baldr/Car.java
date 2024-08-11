package io.baldr;

public class Car {
    private String name;

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
        return null;
    }

    public void setEngine(Engine engine) {

    }

	public void start() {
        getEngine().setStarted(true);
	}
}
