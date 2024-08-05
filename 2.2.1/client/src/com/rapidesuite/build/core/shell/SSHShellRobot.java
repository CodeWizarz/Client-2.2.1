package com.rapidesuite.build.core.shell;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.core.action.SSHAction;
import com.rapidesuite.client.common.util.FileUtils;

public class SSHShellRobot
{
	private BuildMain BuildMain;
	private SSHTerminal terminal;
	public SSHAction at;
	private String userName;
	private String password;
	private int port;
	private String host;
	public static String prompt = "$";
	public static boolean streamClosedAfterFinish;
	
	private SSHClient ssh;
	private Session.Shell shell;
	private Thread outputCopier, errorCopier;

	public SSHShellRobot(BuildMain BuildMain, String host, int port, String userName, String password, SSHAction at) throws Exception {
		this.BuildMain = BuildMain;
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		terminal = new SSHTerminal("SSH terminal", this);
		this.at = at;
	}

	public synchronized void connect() {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.connect(host, port);
			ssh.authPassword(userName, password);
			Session session = ssh.startSession();
			session.allocateDefaultPTY();
			shell = session.startShell();
			outputCopier = copierThread(false);
			errorCopier = copierThread(true);
			outputCopier.start();
			errorCopier.start();
			while ( getTerminalText().indexOf(prompt) == -1 ) {
				com.rapidesuite.client.common.util.Utils.sleep(1000);
			}
		} catch ( Exception ex ) {
			FileUtils.printStackTrace(ex);
		}
	}

	public Thread copierThread(final boolean useErrorStream) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				InputStream stream = null;
				try {
					if(useErrorStream) {		
						stream = shell.getErrorStream();
					} else {
						stream = shell.getInputStream();
					}
					
					while(true) {
						final String c = new String(new byte[] { new Integer(stream.read()).byteValue() });
						if(c != null) {
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									setReceivedText(c);
								}
							});
						}
					}
				} catch(Throwable t) {
					FileUtils.printStackTrace(t);
				} finally {
					IOUtils.closeQuietly(stream);
				}
			}
		});
	}

	public void disconnect() {
		if(outputCopier != null) {
			outputCopier.interrupt();
		}
		if(errorCopier != null) {
			errorCopier.interrupt();
		}
		if ( shell != null ) {
			try {
				shell.close();
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	public void sendText(String text)
	{
		OutputStream cmdStream = null;
		try
		{
			cmdStream = shell.getOutputStream();
			if(StringUtils.isBlank(text)) {
				text = "\n";
				cmdStream.write(text.getBytes());
			} else if(StringUtils.isNotBlank(text)){
				text = propertiesReplace(text);
				cmdStream.write((text + "\n").getBytes());
			}
			cmdStream.flush();
			com.rapidesuite.client.common.util.Utils.sleep(1000);
		} catch ( Throwable t ) {
			FileUtils.printStackTrace(t);
			IOUtils.closeQuietly(cmdStream);
		}
	}
	
	private String propertiesReplace(String original)
	{
		Map<String, String> props = BuildMain.getEnvironmentProperties();
		if ( props != null )
		{
			Iterator<String> it = props.keySet().iterator();
			while ( it.hasNext() )
			{
				String key = it.next();
				String value = props.get(key);
				String tmp = key.replaceAll(" ", "_");
				String keyword = "$" + tmp;
				String keySearch = "\\$" + tmp;
				if ( original.indexOf(keyword) != -1 )
				{
					original = original.replaceAll(keySearch, value);
				}
			}
		}
		return original;
	}

	public void executeQuestions(InputStream in) throws Exception
	{
		TelnetScriptHandler handler = new TelnetScriptHandler(in, this.terminal, this);
		prompt = ((QA) handler.getAllQA().getStructure().get(0)).getQuestion();
		handler.drill(0, handler.getAllQA());
	}

	public void setReceivedText(String text)
	{
		terminal.setReceivedText(text);
	}

	public void fireEvent()
	{
		if ( at != null )
		{
			at.fireActionCompletedEvent();
		}
	}

	public String getTerminalText()
	{
		return terminal.receiveArea.getText();
	}

	public Session.Shell getSession()
	{
		return shell;
	}

	public SSHClient getClient()
	{
		return ssh;
	}
	
	public SSHTerminal getTerminal()
	{
		return terminal;
	}

}