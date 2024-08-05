package com.rapidesuite.build.utils;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.util.FileUtils;

public class ProcessWatchdog implements Runnable {
	
	private Process process = null;
	
	private ProcessWatchdog(Process process) {
		this.process = process;
	}
	
	public static void initializeProcessWatchdog(Process process) {
		Runnable processWatchdog = new ProcessWatchdog(process);
		Thread thread = new Thread(processWatchdog);
		thread.start();
	}

	@Override
	public void run() {
		Assert.notNull(process);
		//execute CommandLineTasksMap.removeByValue once the process terminates
		try {
			process.waitFor();
			int exitCode = process.exitValue();
			FileUtils.println("Browser has exited with exit code: " + exitCode);
			CurrentBrowserTask.eliminateTaskIfTaskEqualsTo(process);
		} catch (InterruptedException e) {
			throw new Error(e);
		}
	}

}
