package hu.bme.mit.requirements.polarion.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WaitingToWriteThread implements Runnable {
	private FileReader fr = null;
	private BufferedReader br = null;
	//private int i = 0;
	private BlockingQueue<String[]> tasks;
	public WaitingToWriteThread(PolarionAdapter simplifiedRequirementHandler) {
		tasks = new LinkedBlockingQueue<String[]>();
	}

	@Override
	public void run() {
		File stopFile = new File("resources/incremental.stop");
		while (stopFile.exists() == false) {
			File lockFile = new File("C:\\Users\\admin\\Desktop\\testIncr\\.lock");
			if (lockFile.exists()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				try {
					lockFile.createNewFile();
					File frFile = new File("C:\\Users\\admin\\Desktop\\testIncr\\testIncrtest.txt");
					if (frFile.exists()) {
						fr = new FileReader("C:\\Users\\admin\\Desktop\\testIncr\\testIncrtest.txt");
						br = new BufferedReader(fr);

						while (true) {
							String line = br.readLine();
							if (line == null)
								break;
							String[] methodNameAndUri = line.split("##");
							tasks.add(methodNameAndUri);
						}
					}
					lockFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null) {
							br.close();
							// clear testIncrtest.txt
							PrintWriter pw = new PrintWriter("C:\\Users\\admin\\Desktop\\testIncr\\testIncrtest.txt");
							pw.close();
						}
						Thread.sleep(5000);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		String[] stop = {"stop"};
		tasks.add(stop);
	}

	public BlockingQueue<String[]> getTasks() {
		return tasks;
	}

	public void clearTasks() {
		tasks.clear();
	}
}
