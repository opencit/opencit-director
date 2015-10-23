/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DirectorUtil {

	private static final Logger log = LoggerFactory
			.getLogger(DirectorUtil.class);

	public static String createTar(String imageDir, String imageName,
			String trustPolicyName, String tarLocation, String tarName)
			throws IOException {
		String imagePathDelimiter = "/";

		String command = "tar -cf " + tarLocation + tarName + " -C " + imageDir
				+ " " + imageName + " " + trustPolicyName;
		// / String tarName = imageName + "-" + new
		// SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".tar";
		// / log.debug(genrateTP.executeShellCommand("tar -cf " + imageTPDir +
		// imagePathDelimiter + tarName + " -C " + imageTPDir + " " + imageName
		// + " " + trustpolicyName));
		System.out.println("Inside create Tar...................command::"
				+ command);
		log.debug(executeShellCommand("cd " + imageDir));
		log.debug(executeShellCommand("pwd "));
		log.debug(executeShellCommand(command));

		return tarLocation + tarName;
	}

	public static void createCopy(String absoluteImagePath,
			String absoluteModifiedImagePath) throws IOException {
		String imagePathDelimiter = "/";

		String command = "cp " + absoluteImagePath + " "
				+ absoluteModifiedImagePath;

		System.out.println("Inside createCopy..................command::"
				+ command);
		log.debug("Inside createCopy..................command::" + command);

		log.debug(executeShellCommand(command));

	}


	public static String getMountPath(String imageId) {
		StringBuilder sb = new StringBuilder(Constants.mountPath);
		sb.append(imageId);
		return sb.toString();
	}

	public static String getDirectorId() {
		Configuration configuration;
		try {
			configuration = ConfigurationFactory.getConfiguration();
			return configuration.get(Constants.DIRECTOR_ID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String computeHash(MessageDigest md, File file) {
		return null;

	}

	public static String getSymbolicLink(String filePath) {
		return filePath;

	}

	public static String executeShellCommand(String command) {
		log.debug("Command to execute is:" + command);
		String[] cmd = { "/bin/sh", "-c", command };
		Process p;
		// / int exitCode=1;
		String excludeList = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
			}
			if (!result.toString().equals("")) {
				excludeList = result.toString();
				excludeList = excludeList.replaceAll("\\n$", "");
			}
			// log.debug("Result of execute command: "+result);
		} catch (InterruptedException ex) {
			log.error(null, ex);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return excludeList;
	}

	public static int callExec(String command) {

		StringBuilder output = new StringBuilder();
		int exitCode = 12345;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			exitCode = p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
		} catch (InterruptedException | IOException ex) {
			log.error(null, ex);
		}
		log.debug(output.toString());
		log.trace("Exec command output : " + output.toString());
		return exitCode;

	}


	public static int executeCommandInExecUtil(String command,
			String... args) throws IOException {
		Result result = ExecUtil.execute(command, args);
		return result.getExitCode();
	}
}
