package com.rapidesuite.client.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.utils.KernelExtra;
import com.rapidesuite.client.common.PlatformNotSupportedError;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 *
 * @author Edwin Jose Palathinkal and Fajrian Yunus
 */
public class Portability {

	private static final int SIGKILL = 9;
	private static final int SYNCHRONIZE = 0x00100000;
	private static final int PROCESS_TERMINATE = 0x0001;
	private static final int PROCESS_SUSPEND_RESUME = 0x0800;
	private static final int PROCESS_QUERY_INFORMATION = 0x0400;
	private static final int WCONTINUED = 0xffff;
	private static final int STILL_ALIVE = 259;
	private static final String CMD_LIST_PID_FOR_PPID = "ps -o pid --ppid=%d";

	public interface POSIX extends Library {

		public int waitpid(int pid, int[] status, int flags);

		public int kill(int pid, int signal);
	}

	/*
	 * On Linux make sure this method is called after 10 ms after a kill, or
	 * else it will report that the process is still alive.
	 */
	public static boolean isAlive(Process process) {
		return isAlive(toPid(process));
	}

	/*
	 * On Linux make sure this method is called after 10 ms after a kill, or
	 * else it will report that the process is still alive.
	 */
	public static boolean isAlive(int pid) {
		if (Platform.isWindows()) {
			HANDLE handle = pidToHandle(pid);
			IntByReference reference = new IntByReference();
			boolean success = Kernel32.INSTANCE.GetExitCodeProcess(handle,
					reference);
			if (success) {
				return reference.getValue() == STILL_ALIVE;
			}
			return false;
		} else if (Platform.isLinux()) {
			int[] status = new int[1];
			POSIX posix = (POSIX) Native.loadLibrary("c", POSIX.class);
			int returnValue = posix.waitpid(pid, status, 1);
			if (returnValue == -1) {
				return false;
			}

			if (returnValue == pid) {
				return (status[0] == WCONTINUED);
			}

			if (returnValue == 0) {
				return true;
			}
		} else {
			throw new PlatformNotSupportedError();
		}
		return false;
	}

	public static List<Integer> getChildren(Process process) {
		int pid = toPid(process);
		return getChildren(pid);
	}

	public static List<Integer> getChildren(int pid) {
		try {
			if (Platform.isWindows()) {
				HANDLE handle = pidToHandle(pid);
				ArrayList<Integer> result = new ArrayList<Integer>();
				WinNT.HANDLE hSnap = Kernel32.INSTANCE
						.CreateToolhelp32Snapshot(
								KernelExtra.TH32CS_SNAPPROCESS, new DWORD(0));
				PROCESSENTRY32.ByReference ent = new PROCESSENTRY32.ByReference();
				if (!Kernel32.INSTANCE.Process32First(hSnap, ent)) {
					return result;
				}
				do {
					if (ent.th32ParentProcessID.intValue() == pid) {
						result.add(ent.th32ProcessID.intValue());
					}
				} while (Kernel32.INSTANCE.Process32Next(hSnap, ent));
				Kernel32.INSTANCE.CloseHandle(hSnap);
				return result;
			} else if (Platform.isLinux()) {
				String cmd = String.format(CMD_LIST_PID_FOR_PPID, pid);
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader input = null;
				InputStream processInputStream = null;
				List<Integer> output = new ArrayList<Integer>();
				try {
					processInputStream = p.getInputStream();
					input = new BufferedReader(
							new InputStreamReader(processInputStream));
					String line;
					input.readLine();
					while ((line = input.readLine()) != null) {
						line = line.trim();
						// the last line might be empty
						if (!line.equals("")) {
							// if there is parsing error, let an error be thrown
							Integer childId = Integer.parseInt(line);
							output.add(childId);
						}
					}
				} finally {
					IOUtils.closeQuietly(input);
					IOUtils.closeQuietly(processInputStream);
				}
				return output;

			} else {
				throw new PlatformNotSupportedError();
			}
		} catch (Throwable t) {
			throw new Error(t);
		}
	}

	@SuppressWarnings("rawtypes")
    public static Map<Integer, Map> getDescendantTree(int pid) {
		Map<Integer, Map> output = null;
		Set<Integer> pids = new HashSet<Integer>();
		output = doGetDescendantTree(pid, pids);
		Assert.notNull(output);
		return output;
	}

	//FIXME: refactor this (and its dependencies) to use tree
	@SuppressWarnings("rawtypes")
    private static Map<Integer, Map> doGetDescendantTree(final int pid, final Set<Integer> traversedNodes) {
		List<Integer> children = getChildren(pid);

		if (children.isEmpty()) {
			Map<Integer, Map> noChildren = new HashMap<Integer, Map>();
			return noChildren;
		} else {
			Map<Integer, Map> grandChildren = new HashMap<Integer, Map>();
			for (Integer i : children) {
				if (traversedNodes.contains(i)) {
					continue; //ignore this node
				} else {
					traversedNodes.add(i);
				}
				grandChildren.put(i, doGetDescendantTree(i, traversedNodes));
			}
			return grandChildren;
		}
	}

    public static Process startProcess(String command)
    {
        return startProcess(command, null, null, command);
    }

	public static Process startProcess(String command, String[] envp, File dir, String commandStringToLog) {
		try {
			FileUtils.println("Running: command = " + commandStringToLog);
			if ( null != envp )
			{
			    FileUtils.println("Running: envp = " + Arrays.asList(envp));
			}
            if ( null != dir )
            {
                FileUtils.println("Running: dir = " + dir.getAbsolutePath());
            }
			Process p = Runtime.getRuntime().exec(command, envp, dir);

			//This try is meant to cause an exception if the process is running.
			try {
				int exitCode = p.exitValue();
				FileUtils.println("Process: " + commandStringToLog + " has exited with exit code: " + exitCode);
			} catch(IllegalThreadStateException e) {
			    FileUtils.println("Process: " + commandStringToLog + " is running.");
			}
			return p;
		} catch (Throwable t) {
			throw new Error("startProcess FAILED for command = " + commandStringToLog, t);
		}
	}

	public static boolean kill(int pid) {
		try {
			if (Platform.isWindows()) {
				HANDLE handle = pidToHandle(pid);
				boolean result = Kernel32.INSTANCE.TerminateProcess(handle, 0);
				return result;
			} else if (Platform.isLinux()) {
				POSIX posix = (POSIX) Native.loadLibrary("c", POSIX.class);
				int result = posix.kill(pid, SIGKILL);
				return result != -1;
			} else {
				throw new PlatformNotSupportedError();
			}
		} catch (Throwable t) {
			throw new Error(t);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean killDescendants(Map<Integer, Map> tree) {
		boolean success = true;
		for (Map.Entry<Integer, Map> pair : tree.entrySet()) {
			HashMap<Integer, Map> value = (HashMap<Integer, Map>) pair.getValue();
			if (value.isEmpty()) {
				success &= kill(pair.getKey());
			} else {
				success &= killDescendants(value);
			}
		}
		return success;
	}

	public static boolean killProcessAndDescendants(Process process) {
		int pid = Portability.toPid(process);
		boolean output = Portability.killProcessAndDescendants(pid);
		return output;
	}

	public static boolean killProcessAndDescendants(int pid) {
		if (killDescendants(getDescendantTree(pid))) {
			return kill(pid);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
    public static void toString(HashMap<Integer, Object> tree, int level, StringBuilder builder) {
		if (tree == null) {
			return;
		}

		for (Map.Entry<Integer, Object> pair : tree.entrySet()) {
			for (int i = 0; i < level; i++) {
				builder.append('\t');
			}
			builder.append(pair.getKey());
			builder.append('\n');
			if (pair.getValue() != null) {
				if (pair.getValue() instanceof HashMap) {
					HashMap<Integer, Object> value = (HashMap<Integer, Object>) pair.getValue();
					toString(value,
							level + 1, builder);
				}
			}
		}
	}

	private static HANDLE pidToHandle(int pid) {
		HANDLE handle = Kernel32.INSTANCE.OpenProcess(PROCESS_QUERY_INFORMATION
		| PROCESS_SUSPEND_RESUME
		| PROCESS_TERMINATE
		| SYNCHRONIZE, false, pid);
		if (handle == null) {
			throw new Error(
					"OpenProcess failed: "
							+ Kernel32Util
									.formatMessageFromLastErrorCode(Kernel32.INSTANCE
											.GetLastError()));
		}
		return handle;
	}

	public static int toPid(Process process) {
		try {
			if (Platform.isWindows()) {
				Field field = process.getClass().getDeclaredField("handle");
				field.setAccessible(true);
				int pid;
				pid = (int) Kernel32.INSTANCE.GetProcessId(new HANDLE(
						new Pointer(field.getLong(process))));
				return pid;
			} else if (Platform.isLinux()) {
				Field field = process.getClass().getDeclaredField("pid");
				field.setAccessible(true);
				return field.getInt(process);
			} else {
				throw new PlatformNotSupportedError();
			}
		} catch (Throwable t) {
			throw new Error(t);
		}
	}

	public static List<String> listWindowNamesOfDescendants(int pid) {
		Assert.isTrue(Platform.isWindows());
		List<Integer> descendants = flatten(getDescendantTree(pid));
		List<String> titles = new ArrayList<String>();
		for(int i : descendants) {
			titles.addAll(listWindowNames(i));
		}
		return titles;
	}

	public static List<String> listWindowNamesOfDescendants(Process task) {
		Assert.isTrue(Platform.isWindows());
		return listWindowNamesOfDescendants(toPid(task));
	}

	public static List<String> listWindowNames(Process process) {
		Assert.isTrue(Platform.isWindows());
		final int pid = toPid(process);
		return listWindowNames(pid);
	}

	public static List<String> listWindowNames(final int pid) {
		Assert.isTrue(Platform.isWindows());
		final List<String> titles = new ArrayList<String>();
		User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
			@Override
			public boolean callback(HWND hWnd, Pointer pntr) {
				IntByReference reference = new IntByReference();
				User32.INSTANCE.GetWindowThreadProcessId(hWnd, reference);
				if (reference.getValue() == pid) {
					char[] windowText = new char[512];
					User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
					String wText = Native.toString(windowText);
					if (!wText.isEmpty()) {
						titles.add(wText);
					}
				}
				return true;
			}
		}, null);
		return titles;
	}

	@SuppressWarnings("rawtypes")
    private static List<Integer> flatten(Map<Integer, Map> tree) {
		if(tree.isEmpty()) {
			return new ArrayList<Integer>();
		} else {
			List<Integer> list = new ArrayList<Integer>();
			list.addAll(tree.keySet());
			for(Object o : tree.values()) {
				@SuppressWarnings("unchecked")
                Map<Integer, Map> map = (Map<Integer, Map>) o;
				list.addAll(flatten(map));
			}
			return list;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean anyLinuxSubprocessHasNetworkConnection(Process rootProcess) {
		Assert.notNull(rootProcess, "anyLinuxSubprocessHasNetworkConnection argument must not be null");
		Assert.isTrue(Platform.isLinux(), "anyLinuxSubprocessHasNetworkConnection method is only for Linux");

		boolean output = false;
		final String lsofCommandFormat = "lsof -nl -p %d";

		Map<Integer, Map> processesTree = Portability.getDescendantTree(Portability.toPid(rootProcess));

		//breadth first search traversal
		List<Map<Integer, Map>> tempTrees = new ArrayList<Map<Integer, Map>>();
		tempTrees.add(processesTree);
		do {
			Assert.notEmpty(tempTrees);
			for (Map.Entry<Integer, Map> entry : tempTrees.get(0).entrySet()) {
			    Assert.notNull(entry.getValue(), "Tree node must not be null");
			    tempTrees.add((Map<Integer, Map>) entry.getValue());

			    Integer pid = entry.getKey();
			    Assert.notNull(pid);
			    Process p = Portability.startProcess(String.format(lsofCommandFormat, pid));
			    InputStream pInputStream = null;
			    BufferedReader input = null;
			    try {
				    pInputStream = p.getInputStream();
				    input = new BufferedReader(new InputStreamReader(pInputStream));

				    String line;
				    input.readLine();
					while ((line = input.readLine()) != null) {
						if (line.matches(".*(TCP|UDP).*")) {
							output = true;
							break;
						}
					}
			    } catch (Throwable t) {
			    	throw new Error(t);
			    } finally {
			    	IOUtils.closeQuietly(input);
			    	IOUtils.closeQuietly(pInputStream);
			    }

		    	if (output) {
		    		break;
		    	}

			}
			Assert.notEmpty(tempTrees);
			tempTrees.remove(0);
		} while(!tempTrees.isEmpty() && !output);


		return output;
	}
}
