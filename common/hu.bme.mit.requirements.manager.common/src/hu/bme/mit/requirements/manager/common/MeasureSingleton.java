package hu.bme.mit.requirements.manager.common;

import java.util.ArrayList;
import java.util.List;

public class MeasureSingleton {
	private static MeasureSingleton instance = null;
	private static final int initialCapacity = 600;
	public final List<MyPair> timeStorage = new ArrayList<MyPair>(initialCapacity);
	public int items = 0;
	public int containments = 0;
	public int abstractions = 0;
	
	// 1 instantiation
	private MeasureSingleton() {
		
	}
	
	public static MeasureSingleton getInstance() {
		if (instance == null) {
			instance = new MeasureSingleton();
		}
		
		return instance;
	}
}
