package hu.bme.mit.requirements.manager.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import org.junit.Test;

import hu.bme.mit.requirements.dng.sandbox.DngAdapter;
import hu.bme.mit.requirements.polarion.sandbox.PolarionAdapter;

public class Main {
	public static final double oneSecInNanosec = 1.0E09;
	
	@Test
	public void test() {
		//testDngOslcProject("dngTestProjectArea");
	}
	
	public void dngTests() {
		testDng1("dngTestProject_1");
		testDng2("dngTestProject_2");
		testDng4("dngTestProject_4");
		testDng8("dngTestProject_8");
	}
	
	public void polarionreqTests() {
		testPolarionReq1("polarionreqTestProject_1");
		testPolarionReq2("polarionreqTestProject_2");
		testPolarionReq4("polarionreqTestProject_4");
		testPolarionReq8("polarionreqTestProject_8");
		//testPolarionReqIncremental("polarionIncrementalTest");
	}
	
	public void clearMeasuredValues() {
		MeasureSingleton.getInstance().timeStorage.clear();
		MeasureSingleton.getInstance().items = 0;
		MeasureSingleton.getInstance().containments = 0;
		MeasureSingleton.getInstance().abstractions = 0;
	}
	
	public void testDngOslcProject(String modelName) {
		clearMeasuredValues();
		
		// measurement1 start
		long start = System.nanoTime();
		
		RequirementManager reqManagerDng = new DngAdapter();
		String[] dngProjectNames = { "Test Project Area" };
		reqManagerDng.createRequirementModel("models/dng/" + modelName + ".uml", dngProjectNames);
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_ENTIRE_PROCESS, elapsedTime));
		
		writeResultToFile("results/dng/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "DNG");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 1
	 */
	public void testDng1(String modelName) {
		clearMeasuredValues();
		
		// measurement1 start
		long start = System.nanoTime();
		
		RequirementManager reqManagerDng = new DngAdapter();
		String[] dngProjectNames = { "dngTestProject1" };
		reqManagerDng.createRequirementModel("models/dng/" + modelName + ".uml", dngProjectNames);
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_ENTIRE_PROCESS, elapsedTime));
		
		writeResultToFile("results/dng/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "DNG");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 2
	 */
	public void testDng2(String modelName) {
		clearMeasuredValues();
		
		// measurement1 start
		long start = System.nanoTime();
		
		RequirementManager reqManagerDng = new DngAdapter();
		String[] dngProjectNames = { "dngTestProject1", "dngTestProject2" };
		reqManagerDng.createRequirementModel("models/dng/" + modelName + ".uml", dngProjectNames);
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_ENTIRE_PROCESS, elapsedTime));
		
		writeResultToFile("results/dng/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "DNG");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 4
	 */
	public void testDng4(String modelName) {
		clearMeasuredValues();
		
		// measurement1 start
		long start = System.nanoTime();
		
		RequirementManager reqManagerDng = new DngAdapter();
		String[] dngProjectNames = { "dngTestProject1", "dngTestProject2", "dngTestProject3", "dngTestProject4" };
		reqManagerDng.createRequirementModel("models/dng/" + modelName + ".uml", dngProjectNames);
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_ENTIRE_PROCESS, elapsedTime));
		
		writeResultToFile("results/dng/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "DNG");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 8
	 */
	public void testDng8(String modelName) {
		clearMeasuredValues();
		
		// measurement1 start
		long start = System.nanoTime();
		
		RequirementManager reqManagerDng = new DngAdapter();
		String[] dngProjectNames = { "dngTestProject1", "dngTestProject2", "dngTestProject3", "dngTestProject4",
										"dngTestProject5", "dngTestProject6", "dngTestProject7", "dngTestProject8"};
		reqManagerDng.createRequirementModel("models/dng/" + modelName + ".uml", dngProjectNames);
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_ENTIRE_PROCESS, elapsedTime));
		
		writeResultToFile("results/dng/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "DNG");
		System.out.println("Done.");
	}
	
	public void testDng() {
		clearMeasuredValues();
		
		// measurement1 start
		long start = System.nanoTime();
		
		RequirementManager reqManagerDng = new DngAdapter();
		//String[] dngProjectNames = { "Test Project Area", "Automated Meter Reader (water)" };
		String[] dngProjectNames = { "JKE Banking" };
		reqManagerDng.createRequirementModel("models/dng/dngModel4.uml", dngProjectNames);
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_ENTIRE_PROCESS, elapsedTime));
		
		writeResultToFile("results/dng/dngResult4.txt", MeasureSingleton.getInstance().timeStorage, "DNG");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 1
	 */
	public void testPolarionReq1(String modelName) {
		clearMeasuredValues();
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		RequirementManager reqManagerPol = new PolarionAdapter();
		String[] polarionProjectIds = { "polarionreqTestProject1" };
		reqManagerPol.createRequirementModel("models/polarionreq/" + modelName + ".uml", polarionProjectIds);
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_ENTIRE_PROCESS, elapsedTime2));
		
		writeResultToFile("results/polarionreq/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "Polarion");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 2
	 */
	public void testPolarionReq2(String modelName) {
		clearMeasuredValues();
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		RequirementManager reqManagerPol = new PolarionAdapter();
		String[] polarionProjectIds = { "polarionreqTestProject1", "polarionreqTestProject2" };
		reqManagerPol.createRequirementModel("models/polarionreq/" + modelName + ".uml", polarionProjectIds);
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_ENTIRE_PROCESS, elapsedTime2));
		
		writeResultToFile("results/polarionreq/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "Polarion");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 4
	 */
	public void testPolarionReq4(String modelName) {
		clearMeasuredValues();
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		RequirementManager reqManagerPol = new PolarionAdapter();
		String[] polarionProjectIds = { "polarionreqTestProject1", "polarionreqTestProject2", "polarionreqTestProject3", "polarionreqTestProject4" };
		reqManagerPol.createRequirementModel("models/polarionreq/" + modelName + ".uml", polarionProjectIds);
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_ENTIRE_PROCESS, elapsedTime2));
		
		writeResultToFile("results/polarionreq/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "Polarion");
		System.out.println("Done.");
	}
	
	/**
	 * model size = 8
	 */
	public void testPolarionReq8(String modelName) {
		clearMeasuredValues();
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		RequirementManager reqManagerPol = new PolarionAdapter();
		String[] polarionProjectIds = { "polarionreqTestProject1", "polarionreqTestProject2", "polarionreqTestProject3", "polarionreqTestProject4",
										"polarionreqTestProject5", "polarionreqTestProject6", "polarionreqTestProject7", "polarionreqTestProject8"};
		reqManagerPol.createRequirementModel("models/polarionreq/" + modelName + ".uml", polarionProjectIds);
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_ENTIRE_PROCESS, elapsedTime2));
		
		writeResultToFile("results/polarionreq/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "Polarion");
		System.out.println("Done.");
	}
	
	public void testPolarionReqIncremental(String modelName) {
		clearMeasuredValues();
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		RequirementManager reqManagerPol = new PolarionAdapter();
		String[] polarionProjectIds = { "testproject3" };
		reqManagerPol.createRequirementModel("models/polarionreq/" + modelName + ".uml", polarionProjectIds);
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_ENTIRE_PROCESS, elapsedTime2));
		
		reqManagerPol.doIncrementalModelUpdate();
		
		writeResultToFile("results/polarionreq/" + modelName + ".txt", MeasureSingleton.getInstance().timeStorage, "Polarion");
		System.out.println("Done.");
	}
	
	public void testPolarionReq() {
		clearMeasuredValues();
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		RequirementManager reqManagerPol = new PolarionAdapter();
		String[] polarionProjectIds = { "testproject", "testproject2" };
		reqManagerPol.createRequirementModel("models/polarionreq/polarionReqModel2.uml", polarionProjectIds);
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_ENTIRE_PROCESS, elapsedTime2));
		
		//reqManagerPol.doIncrementalModelUpdate();
		
		writeResultToFile("results/polarionreq/polarionreqResult2.txt", MeasureSingleton.getInstance().timeStorage, "Polarion");
		System.out.println("Done.");
	}
	
	public void writeResultToFile(String fileName, List<MyPair> result, String dngOrPolarionReq) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(fileName, true);
			bw = new BufferedWriter(fw);
			
			DecimalFormat df = new DecimalFormat("########.########");
			
			// clear the file: fileName
			PrintWriter pw = new PrintWriter(fileName);
			pw.close();
			
			for (MyPair mp : result) {
				// value in seconds
				bw.write(mp.getName() + "\t" + df.format(mp.getValue() / oneSecInNanosec));
				bw.newLine();
			}
			bw.write(dngOrPolarionReq + " queried items\t" + MeasureSingleton.getInstance().items);
			bw.newLine();
			bw.write(dngOrPolarionReq + " containments\t" + MeasureSingleton.getInstance().containments);
			bw.newLine();
			bw.write(dngOrPolarionReq + " abstractions\t" + MeasureSingleton.getInstance().abstractions);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
