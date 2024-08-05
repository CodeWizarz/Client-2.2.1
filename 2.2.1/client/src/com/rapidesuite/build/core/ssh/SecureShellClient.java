package com.rapidesuite.build.core.ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.RemoteDirectory;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;

public class SecureShellClient
{
	private static final int ALL_PERMISSIONS = 0777;
	private static final CharSequence ENCRYPTED = "ENCRYPTED";
	public static String SSH_PRIVATE_KEY_FILE_NAME_PROPERTY = "SSH_PRIVATE_KEY_FILE_NAME";
	public static String SSH_DEFAULT_PORT = "22";
	private static String userName, password, privateKeyPassphrase;

	private SSHClient ssh;
	private Session session;
	private SFTPClient sftp;
	private String status;

	private SecureShellClient(String hostName, String port, String userName, String password, String privateKeyFileName, boolean isValidation)
	{
		try
		{
			Assert.isTrue(password == null ^ privateKeyFileName == null, "Either password has to not be set or SFTP private key location has to be not set."+
					"Now it is password is set = "+(password!=null)+" and SFTP private key location is set = "+(privateKeyFileName!=null));
			boolean keyBasedAuth = privateKeyFileName != null;
			boolean passphraseNeeded = false;
			if(keyBasedAuth) {
				if (StringUtils.isBlank(privateKeyFileName)) {
					throw new Exception("SFTP private key location is not set");
				}
				File privateKeyFile = new File(privateKeyFileName);
				if (!privateKeyFile.exists()) {
					throw new Exception("SFTP private key location ('"+privateKeyFileName+"') is invalid");
				}
				String privateKeyContent = org.apache.commons.io.FileUtils.readFileToString(privateKeyFile);
				passphraseNeeded = privateKeyContent.contains(ENCRYPTED);
			}

			if(isValidation && Config.isEnvironmentValidationMandatory()) {
				boolean usernamePresent = StringUtils.isNotBlank(userName);
				if(usernamePresent) {
					SecureShellClient.userName = userName;
				} else if (!Config.isAutomatedRun()) {
					String[] credentials = new MyUserInfo().promptKeyboardInteractive("Username", "Username", "Enter your username:", new String[] {"Username"}, new boolean[] {true});
					if(credentials == null || StringUtils.isBlank(credentials[0])) {
						SecureShellClient.userName = null;
						throw new Exception("Validation stopped, because of invalid username");
					} else {
						SecureShellClient.userName = credentials[0];
					}
				}

				if(passphraseNeeded) {
					if(Config.containsBuildSshPassphrase()) {
						SecureShellClient.privateKeyPassphrase = Config.getBuildSshPassphrase();
					} else if (!Config.isAutomatedRun()) {
						String[] credentials = new MyUserInfo().promptKeyboardInteractive("Passphrase", "Passphrase", "Enter your passphrase:", new String[] {"Passphrase"}, new boolean[] {false});
						if(credentials == null || StringUtils.isBlank(credentials[0])) {
							SecureShellClient.privateKeyPassphrase = null;
							throw new Exception("Validation stopped, because of invalid passphrase");
						} else {
							SecureShellClient.privateKeyPassphrase = credentials[0];
						}
					}
				} else if(!keyBasedAuth) {
					boolean passwordPresent = StringUtils.isNotBlank(password);
					if(passwordPresent) {
						SecureShellClient.password = password;
					} else if (!Config.isAutomatedRun()) {
						String[] credentials = new MyUserInfo().promptKeyboardInteractive("Password", "Password", "Enter your password:", new String[] {"Password"}, new boolean[] {false});
						if(credentials == null || StringUtils.isBlank(credentials[0])) {
							SecureShellClient.password = null;
							throw new Exception("Validation stopped, because of invalid password");
						} else {
							SecureShellClient.password = credentials[0];
						}
					}
				}
			} else {
				if(!Config.isEnvironmentValidationMandatory()) {
					SecureShellClient.userName = userName;
					SecureShellClient.password = password;
					if(passphraseNeeded) {
						SecureShellClient.privateKeyPassphrase = Config.getBuildSshPassphrase();
					}
				}

				if(StringUtils.isBlank(SecureShellClient.userName)) {
					SecureShellClient.userName = null;
					throw new Exception("SSH connection failed, because of invalid username");
				}
				if(passphraseNeeded) {
					if(StringUtils.isBlank(SecureShellClient.privateKeyPassphrase)) {
						SecureShellClient.privateKeyPassphrase = null;
						throw new Exception("SSH connection failed, because of invalid passphrase");
					}
				} else if(!keyBasedAuth) {
					if(StringUtils.isBlank(SecureShellClient.password)) {
						SecureShellClient.password = null;
						throw new Exception("SSH connection failed, because of invalid password");
					}
				}
			}

			int portNumber = new Integer(port).intValue();

			String tmp = "";
			if ( StringUtils.isNotBlank(privateKeyFileName) )
			{
				tmp = " private key file.";
			}
			else
			{
				tmp = " password.";
			}

			FileUtils.println("SSH: connecting to '" + SecureShellClient.userName + "@" + hostName + "' using " + tmp);


			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			try
			{
		        ssh.connect(hostName, portNumber);
			}
			catch(net.schmizz.sshj.transport.TransportException te)
			{
			    if ( te.getMessage().equals("Incorrect identification: line too long") )
			    {
			        throw new Exception("SSH connection failed - Server probably does not support SSH.  Please try using FTP.", te);
			    }
			    else
			    {
			        throw te;
			    }
			}

			try
			{
				if (keyBasedAuth) {
					OpenSSHKeyFile keyFile = new OpenSSHKeyFile();
					File privateKeyFile = new File(privateKeyFileName);
					if(passphraseNeeded) {
						PasswordFinder finder = new PasswordFinder() {

							@Override
							public char[] reqPassword(Resource<?> arg0) {
								return SecureShellClient.privateKeyPassphrase.toCharArray();
							}

							@Override
							public boolean shouldRetry(Resource<?> arg0) {
								return false;
							}
						};
						keyFile.init(privateKeyFile, finder);
					} else {
						keyFile.init(privateKeyFile);
					}
					ssh.authPublickey(SecureShellClient.userName, keyFile);
				} else {
					ssh.authPassword(SecureShellClient.userName, SecureShellClient.password);
				}
			}
			catch(Exception e)
			{
				final String errorMessage;
				if (keyBasedAuth) {
					errorMessage = "ERROR AUTHENTICATING - Please ensure you are using the correct private encryption key and the correct passphrase";
				} else {
					errorMessage = "ERROR AUTHENTICATING - Please check the password";
				}
				throw new Exception(errorMessage, e);
			}

			session = ssh.startSession();
			sftp = ssh.newSFTPClient();
			status = SwiftBuildConstants.CONNECTION_SUCCESS_MESSAGE;
		}
		catch ( Exception ex )
		{
			FileUtils.println("SSH: cannot connect to the server.");
			FileUtils.printStackTrace(ex);
			status = ex.getMessage();
			SecureShellClient.userName = null;
			SecureShellClient.password = null;
			throw new Error(ex);
		}
	}

	public String getStatus()
	{
		return status;
	}

	public boolean isConnected()
	{
		return ssh.isConnected();
	}

	public void close()
	{
		try {
			ssh.disconnect();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public SSHClient getInstance()
	{
		return ssh;
	}

	public String pwd()
	{
		try
		{
			final Command cmd = session.exec("pwd");
			String result = IOUtils.readFully(cmd.getInputStream()).toString();
			cmd.join(Config.getBuildSshSocketTimeoutSeconds(), TimeUnit.SECONDS);
			if(cmd.getExitStatus() != 0) {
				throw new Exception(result);
			}
			return result;
		} catch ( Exception ex ) {
			throw new Error(ex);
		}
	}

	public boolean cd(String dir) throws Exception
	{
		try
		{
			FileUtils.println("SSH: trying changing directory: '" + dir + "'");
			RemoteDirectory rDir = sftp.getSFTPEngine().openDir(dir);			
			String rDirPath = rDir.getPath();
			if(!rDirPath.equals(dir)) {
				throw new Exception("SFTP: Changing directory - changed directory path doesn't match.");
			}
			FileUtils.println("SSH: directory changed.");
			return true;
		}
		catch ( Exception ex )
		{
			FileUtils.println("SSH: error changing directory.");
			FileUtils.printStackTrace(ex);
			return false;
		}
	}

	public void get(String remoteFolder, String localDirectory, String fileName) throws Exception
	{
		try {
			new File(localDirectory).mkdirs();
			String remotePath = remoteFolder + UtilsConstants.FORWARD_SLASH + fileName;
			if(sftp.statExistence(remotePath) != null) {
				sftp.get(remotePath, new FileSystemFile(localDirectory + UtilsConstants.FORWARD_SLASH + fileName));
			} else {
				return;
			}
		} catch ( Exception ex ) {
			FileUtils.printStackTrace(ex);
			throw new Exception("Unable to download the file: '" + fileName + "' : " + ex.getMessage());
		}
	}

	public File get(String remoteFolder, String fileName)
	{
		File file;
		try {
		    File tempDir = new File(Config.getTempFolder(), "ssh");
	        tempDir.mkdirs();
	        file = File.createTempFile("ssh-", ".dat", tempDir);
		} catch ( Exception ex ) {
			FileUtils.printStackTrace(ex);
			throw new Error("Unable to download the file: '" + fileName + "' : " + ex.getMessage());
		}
		
		return get(remoteFolder, fileName, file);
	}
	
	public File get(String remoteFolder, String fileName, File localFile)
	{
		try {
			String remotePath = remoteFolder + UtilsConstants.FORWARD_SLASH + fileName;
			if(sftp.statExistence(remotePath) != null) {
				sftp.get(remotePath, new FileSystemFile(localFile));
			} else {
				return null;
			}
			return localFile;
		} catch ( Exception ex ) {
			FileUtils.printStackTrace(ex);
			throw new Error("Unable to download the file: '" + fileName + "' : " + ex.getMessage());
		}
	}	

	public void put(final InputStream in, final String remoteFolder, final String remoteName) throws Exception
	{
		try {
			FileUtils.println("SSH: trying uploading file: '" + remoteName + "' ...");
			sftp.put(new LocalSourceFile(){

				@Override
				public Iterable<? extends LocalSourceFile> getChildren(
						LocalFileFilter arg0) throws IOException {
					return null;
				}

				@Override
				public InputStream getInputStream() throws IOException {
					return in;
				}

				@Override
				public long getLastAccessTime() throws IOException {
					return 0;
				}

				@Override
				public long getLastModifiedTime() throws IOException {
					return 0;
				}

				@Override
				public long getLength() {
					try {
						return in.available();
					} catch (Exception e) {
						throw new Error(e);
					}
				}

				@Override
				public String getName() {
					return remoteName;
				}

				@Override
				public int getPermissions() throws IOException {
					return ALL_PERMISSIONS;
				}

				@Override
				public boolean isDirectory() {
					return false;
				}

				@Override
				public boolean isFile() {
					return true;
				}

				@Override
				public boolean providesAtimeMtime() {
					return false;
				}
			}, remoteFolder + UtilsConstants.FORWARD_SLASH + remoteName);
			FileUtils.println("SSH: file uploaded.");
		} catch ( Exception ex ) {
			String msg = "SSH: cannot upload file: '" + remoteName + "' .";
			FileUtils.println(msg);
			FileUtils.printStackTrace(ex);
			throw new Exception(msg + " Error:" + ex.getMessage());
		}
	}

	public void delete(String dir, String fileName)
	{
		try {
			sftp.rm(fileName);
		} catch ( Exception ex ) {
			FileUtils.println("Failed to delete file '" + fileName + "'"); 
			FileUtils.printStackTrace(ex);
			
			// because in some cases we are unable to delete the file, but are able to delete with the absolute path.
			// so we are going to try again with the absolute path.
			// leaving the first part as is to not impact the instances where it works fine.
			String pathAndName = dir + UtilsConstants.FORWARD_SLASH + fileName;
			FileUtils.println("trying to delete '" + pathAndName + "'");
			
			try {
				sftp.rm(pathAndName);
			} catch ( Exception ex2 ) {
				FileUtils.println("Failed to delete file '" + pathAndName + "' too."); 
				FileUtils.printStackTrace(ex2);
				
			}
		}
	}

	public long getSize(String fileName)
	{
		try {
			return sftp.lstat(fileName).getSize();
		} catch ( Exception ex ) {
			FileUtils.printStackTrace(ex);
		}
		return -1;
	}

	public static boolean isInteger(String input)
	{
		try {
			Integer.parseInt(input);
			return true;
		} catch ( Exception ex ) {
			FileUtils.printStackTrace(ex);
			return false;
		}
	}
	
	public static class PasswordBasedSecureShellClient extends SecureShellClient {

		public PasswordBasedSecureShellClient(String hostName, String port,
				String userName, String password, boolean isValidation) {
			super(hostName, port, userName, password, null, isValidation);
		}
		
	}
	
	public static class PrivateKeyBasedSecureShellClient extends SecureShellClient {

		public PrivateKeyBasedSecureShellClient(String hostName, String port,
				String userName, String privateKeyFileName, boolean isValidation) {
			super(hostName, port, userName, null, privateKeyFileName, isValidation);
		}
		
	}	

}