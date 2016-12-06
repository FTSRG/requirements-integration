package com.adva.polarion.actionhook.save.title;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WaitingToWriteThread implements Runnable {
	private FileWriter fw = null;
	private BufferedWriter bw = null;
	private boolean hasToRun = true;
	private int i = 0;
	private String uri;
	
	public WaitingToWriteThread(String uri) {
		this.uri = uri;
	}

	@Override
	public void run() {
		while (hasToRun) {
			File lockFile = new File("C:\\Users\\admin\\Desktop\\testIncr\\.lock");
			if (lockFile.exists()) {
				try {
					System.out.println("sleep" + i++);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				try {
					lockFile.createNewFile();
					fw = new FileWriter("C:\\Users\\admin\\Desktop\\testIncr\\testIncrtest.txt", true);
					bw = new BufferedWriter(fw);
					bw.write("save##" + uri);
					bw.newLine();
					hasToRun = false;
					lockFile.delete();
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
	}
}
