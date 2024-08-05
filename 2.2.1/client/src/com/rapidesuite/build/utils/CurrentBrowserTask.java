package com.rapidesuite.build.utils;

import java.util.List;

import com.rapidesuite.client.common.PlatformNotSupportedError;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.Portability;
import com.sun.jna.Platform;

public class CurrentBrowserTask {

	private static Process task = null;

	public synchronized static Process getTask() {
		return task;
	}

	public synchronized static void setTask(Process task) {
		CurrentBrowserTask.task = task;
	}

	public synchronized static boolean eliminateTask() {
		if (CurrentBrowserTask.task != null) {
			final int NUMBER_OF_MAX_BROWSER_KILL_ATTEMPT = 3;
			boolean successful = false;
			for (int i = 0 ; i < NUMBER_OF_MAX_BROWSER_KILL_ATTEMPT ; i++) {
				try {
					successful = Portability.killProcessAndDescendants(CurrentBrowserTask.task);
					break;
				} catch (Error e) {
					if (i == NUMBER_OF_MAX_BROWSER_KILL_ATTEMPT-1) {
						throw e;
					}
				}
			}


			if (successful) {
				CurrentBrowserTask.task = null;
			}
			return successful;
		}
		return false;
	}

	public synchronized static boolean eliminateTaskIfTaskEqualsTo(Process process) {
		if (process != null && CurrentBrowserTask.task != null) {
			if (process.equals(CurrentBrowserTask.task)) {
				return CurrentBrowserTask.eliminateTask();
			}
		}
		return false;
	}

	public synchronized static boolean isActive() {
		if (CurrentBrowserTask.task == null) {
			return false;
		} else {
			//sometimes there is missync between CurrentBrowserTask.task and the true state
			boolean isStillAlive = Portability.isAlive(CurrentBrowserTask.task);
			if (isStillAlive) {
				return true;
			} else {
				CurrentBrowserTask.task = null;
				return false;
			}
		}
	}

	public synchronized static boolean hasActiveForm() {
		if (isActive()) {
			if(Platform.isLinux()) {
				return Portability.anyLinuxSubprocessHasNetworkConnection(CurrentBrowserTask.task);
			} else if(Platform.isWindows()) {
				//in some situations, the applet window and the browser share the same process, thus necessitating check on the browser's title as well
				List<String> browserProcessWindowNames = Portability.listWindowNames(CurrentBrowserTask.task);
				for (String name : browserProcessWindowNames) {
					if (Config.getBuildExpectedFldAppletTitle().equalsIgnoreCase(name)) {
						return true;
					}
				}
				List<String> descendantWindowNames = Portability.listWindowNamesOfDescendants(CurrentBrowserTask.task);
				for(String name : descendantWindowNames) {
					if(name.equalsIgnoreCase(Config.getBuildExpectedFldAppletTitle())) {
						return true;
					}
				}
			} else {
				throw new PlatformNotSupportedError();
			}
		}
		return false;
	}
}
