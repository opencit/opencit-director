/* 
 * SSHManager
 * 
 * @author aakash
 */
package com.intel.director.service.impl;

import java.io.IOException;
import java.io.InputStream;

import com.intel.director.common.Constants;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHManager {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(SSHManager.class);
	private JSch jschSSHChannel;
	private String strUserName;
	private String strConnectionIP;
	private int intConnectionPort;
	private String strPassword;
	private Session sesConnection;
	private int intTimeOut;

	private void doCommonConstructorActions(String userName, String password,
			String connectionIP, String knownHostsFileName) {
		jschSSHChannel = new JSch();

		try {
			jschSSHChannel.setKnownHosts(knownHostsFileName);
		} catch (JSchException jschX) {
			log.error(jschX.getMessage(), jschX);
		}

		strUserName = userName;
		strPassword = password;
		strConnectionIP = connectionIP;
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
			sesConnection = jschSSHChannel.getSession(strUserName,
					strConnectionIP, intConnectionPort);
			sesConnection.setPassword(strPassword);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			sesConnection.setConfig(config);
			sesConnection.connect(intTimeOut);
		} catch (JSchException jschX) {
			String message = jschX.getMessage();
			return message;
		}

		return Constants.SUCCESS;
	}

	public String sendCommand(String command) {
		StringBuilder outputBuffer = new StringBuilder();

		try {
			Channel channel = sesConnection.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			InputStream commandOutput = channel.getInputStream();
			channel.connect();
			int readByte = commandOutput.read();

			while (readByte != 0xffffffff) {
				outputBuffer.append((char) readByte);
				readByte = commandOutput.read();
			}

			channel.disconnect();
			commandOutput.close();
		} catch (IOException ioX) {
			log.error(ioX.getMessage(), ioX);
			return null;
		} catch (JSchException jschX) {
			log.error(jschX.getMessage(), jschX);
			return null;
		}

		return outputBuffer.toString();
	}

	public void close() {
		sesConnection.disconnect();
	}

}
