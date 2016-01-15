/* 
 * SSHManager
 * 
 * @author aakash
 */
package com.intel.director.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHManager {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(SSHManager.class);

	private JSch jschSSHChannel;
	private String strUserName;
	private String strConnectionIP;
	private final int intConnectionPort;
	private String strPassword;
	private Session sesConnection;
	private final int intTimeOut;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;
	private final String knownHostsFile = "~/.ssh/known_hosts";

	private void doCommonConstructorActions(String userName, String password,
			String connectionIP, String knownHostsFileName) {
		jschSSHChannel = new JSch();

		try {
			jschSSHChannel.setKnownHosts(knownHostsFileName);
		} catch (JSchException jschX) {
			log.error("Error setting known hosts", jschX);
		}

		strUserName = userName;
		strPassword = password;
		strConnectionIP = connectionIP;
	}

	public SSHManager(String userName, String password, String connectionIP) {
		doCommonConstructorActions(userName, password, connectionIP,
				knownHostsFile);
		intConnectionPort = 22;
		intTimeOut = 30000;
	}

	public SSHManager(String userName, String password, String connectionIP,
			String knownHostsFileName) {
		doCommonConstructorActions(userName, password, connectionIP,
				knownHostsFileName);
		intConnectionPort = 22;
		intTimeOut = 30000;
	}

	public SSHManager(String userName, String password, String connectionIP,
			String knownHostsFileName, int connectionPort) {
		doCommonConstructorActions(userName, password, connectionIP,
				knownHostsFileName);
		intConnectionPort = connectionPort;
		intTimeOut = 30000;
	}

	public SSHManager(String userName, String password, String connectionIP,
			String knownHostsFileName, int connectionPort,
			int timeOutMilliseconds) {
		doCommonConstructorActions(userName, password, connectionIP,
				knownHostsFileName);
		intConnectionPort = connectionPort;
		intTimeOut = timeOutMilliseconds;
	}

	public String connect() {

		try {
			log.info("inside ssh connect");
			sesConnection = jschSSHChannel.getSession(strUserName,
					strConnectionIP, intConnectionPort);
			log.info("inside ssh connect setup 1");
			sesConnection.setPassword(strPassword);
			log.info("inside ssh connect setup set passwd");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			int timeout = Integer.valueOf(getSessionTimeout()) ;
			log.info("TIMEOUT: "+timeout);
			timeout = timeout * 60 * 1000;
			sesConnection.setTimeout(timeout);
			sesConnection.setConfig(config);
			log.info("inside ssh connect setup set config");
			sesConnection.connect(intTimeOut);
			log.info("inside ssh connect setup connect complete");
		} catch (JSchException jschX) {
			log.error("Error connectin to remote host "+strConnectionIP, jschX);
			String message = jschX.getMessage();
			return message;
		}

		return Constants.SUCCESS;
	}

	
	
	private String getSessionTimeout() {
		log.info("inside getting session timeout");
		String timeout = "30";

		Properties prop = DirectorUtil.getPropertiesFile("director.properties");
		timeout = prop.getProperty("login.token.expires.minutes", "30");
		log.info("session timeout in happy path = " + timeout);

		log.info("session timeout in default path = " + timeout);
		return timeout;

	}

	private void mkdirs(String path) {
		try {
			String[] folders = path.split("/");
			if (folders[0].isEmpty())
				folders[0] = "/";
			String fullPath = folders[0];
			for (int i = 1; i < folders.length; i++) {
				Vector ls = channelSftp.ls(fullPath);
				boolean isExist = false;
				for (Object o : ls) {
					if (o instanceof LsEntry) {
						LsEntry e = (LsEntry) o;
						if (e.getAttrs().isDir()
								&& e.getFilename().equals(folders[i])) {
							isExist = true;
						}
					}
				}
				if (!isExist && !folders[i].isEmpty()) {
					channelSftp.mkdir(fullPath + folders[i]);
				}
				fullPath = fullPath + folders[i] + "/";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if(channelSftp != null){
			channelSftp.exit();
		}
		sesConnection.disconnect();
	}

	public String testConnect() {
		String result = connect();
		close();
		return result;
	}

	private void setupSftp() throws JSchException {
		String connect = connect();
		if(!Constants.SUCCESS.equals(connect)){
			throw new JSchException("Unable to connect to remote host");
		}
		channel = sesConnection.openChannel("sftp");
		channel.connect();
		log.info("sftp channel opened and connected.");
		channelSftp = (ChannelSftp) channel;

	}

	private void send(List<String> files, String remoteLocation) {

		try {
			
			channelSftp.cd(remoteLocation);
			for (String fileName : files) {
				log.info("Sending file "+fileName);
				File f = new File(fileName);
				channelSftp.put(new FileInputStream(f), f.getName());
				log.info("File transfered successfully to host.");				
			}
		} catch (Exception ex) {
			log.error("Exception found while tranfer the response.", ex);
		} finally {
			channelSftp.exit();
			log.info("sftp Channel exited.");
			channel.disconnect();
			log.info("Channel disconnected.");
			close();
			log.info("Host Session disconnected.");
		}
	}

	public void sendFileToRemoteHost(List<String> files, String remoteLocation)
			throws JSchException {
		setupSftp();
		mkdirs(remoteLocation);
		send(files, remoteLocation);	
		close();
	}

}
