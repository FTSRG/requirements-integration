package hu.bme.mit.requirements.manager.common;

public class MyPair {
	private String name;
	private long value;
	
	public MyPair(String name, long value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public long getValue() {
		return value;
	}
}