package io.baldr;

public class ClassWithSillyConstrcutor {
	private final int i;
	private final Integer integerBoxed;
	private final short s;
	private final Short shortBoxed;
	private final long l;
	private final Long longBoxed;
	private final float f;
	private final Float floatBoxed;
	private final double d;
	private final Double doubleBoxed;

	public ClassWithSillyConstrcutor(int i, Integer integerBoxed, short s, Short shortBoxed, long l, Long longBoxed, float f, Float floatBoxed, double d, Double doubleBoxed) {

		this.i = i;
		this.integerBoxed = integerBoxed;
		this.s = s;
		this.shortBoxed = shortBoxed;
		this.l = l;
		this.longBoxed = longBoxed;
		this.f = f;
		this.floatBoxed = floatBoxed;
		this.d = d;
		this.doubleBoxed = doubleBoxed;
	}

	public int getI() {
		return i;
	}

	public Integer getIntegerBoxed() {
		return integerBoxed;
	}

	public short getS() {
		return s;
	}

	public Short getShortBoxed() {
		return shortBoxed;
	}

	public long getL() {
		return l;
	}

	public Long getLongBoxed() {
		return longBoxed;
	}

	public float getF() {
		return f;
	}

	public Float getFloatBoxed() {
		return floatBoxed;
	}

	public double getD() {
		return d;
	}

	public Double getDoubleBoxed() {
		return doubleBoxed;
	}
}
