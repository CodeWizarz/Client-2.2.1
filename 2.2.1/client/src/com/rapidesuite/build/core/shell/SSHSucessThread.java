package com.rapidesuite.build.core.shell;

public class SSHSucessThread extends Thread
{

	private SSHShellRobot robot;

	public SSHSucessThread(SSHShellRobot robot)
	{
		this.robot = robot;
	}

	public void run()
	{
		/*
		 * try{ SSHScriptThread st=robot.getSSHScriptThread();
		 * com.rapidesuite.build
		 * .core.log.LogWriter.println("STARTING THREAD SUCCESS OBSERVER...");
		 * while(!st.COMPLETED) { com.rapidesuite.client.common.util.Utils.sleep(500); }
		 * 
		 * com.rapidesuite.build.core.log.LogWriter.println(
		 * "SCRIPT COMPLETED... NOTIFYING THE BUILDER WIZARD");
		 * robot.fireEvent(); } catch (InterruptedException e) {
		 * com.rapidesuite.build.core.log.FileUtils.printStackTrace(e); }
		 */
	}

}