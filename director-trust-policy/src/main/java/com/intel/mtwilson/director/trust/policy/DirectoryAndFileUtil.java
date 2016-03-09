/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.trust.policy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.director.common.DirectorUtil;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy.xml.FileMeasurement;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

/**
 * 
 * @author boskisha
 */
public class DirectoryAndFileUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DirectoryAndFileUtil.class);

	private String imageId;

	public DirectoryAndFileUtil(String imageId) {
		this.imageId = imageId;
	}
	
	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public DirectoryAndFileUtil() {
	}

	public String getFilesAndDirectories(String imageId, DirectoryMeasurement dirMeasurement)
			throws FileNotFoundException, IOException {
		// Create find command
		String command = createFindCommand(imageId, dirMeasurement, false);
		// Execute find command and return result
		return executeCommand(command);
	}

	public String getFiles(String imageId, DirectoryMeasurement dirMeasurement)
			throws FileNotFoundException, IOException {
		// Create find command
		String command = createFindCommand(imageId, dirMeasurement, true);
		// Execute find command and return result
		return executeCommand(command);
	}
	/**
	 * This method applies include and exclude criteria on a directory and
	 * returns list of files that satisfies criteria
	 * 
	 * @param imageId
	 *            image ID in process
	 * @param dirMeasurement
	 * @return list of files separated by new line character
	 * @throws FileNotFoundException
	 *             if directory path is not valid it throws exception
	 */
	public String getFiles(String imageId, DirectoryMeasurement dirMeasurement, boolean skipDirectories)
			throws FileNotFoundException, IOException {
		// Create find command
		String command = createFindCommand(imageId, dirMeasurement, skipDirectories);
		// Execute find command and return result
		return executeCommand(command);
	}

	/**
	 * creates linux find command to filter files using include and exclude
	 * regular expression
	 * 
	 * @param mountPath
	 *            mount path where image is mounted
	 * @param directoryPath
	 *            path of a directory whose files needs to be filtered
	 * @param include
	 *            regular expression for the files to be included
	 * @param exclude
	 *            regular expression for the files to be excluded
	 * @return linux find command
	 */
	private String createFindCommand(String imageId,
			DirectoryMeasurement dirMeasurement, boolean skipDirectories) {
		String directoryAbsolutePath = DirectorUtil.getMountPath(imageId)+File.separator+"mount"+dirMeasurement.getPath();
		String include = dirMeasurement.getInclude();
		String exclude = dirMeasurement.getExclude();
		String command = null;
		StringBuilder stringBuilder = new StringBuilder("find " + directoryAbsolutePath);

		if (dirMeasurement.isRecursive() == null) {
			dirMeasurement.setRecursive(false);
		}

		if (dirMeasurement.isRecursive() == false) {
			stringBuilder.append("  -maxdepth 1");
		}
		if (skipDirectories) {
			stringBuilder.append(" ! -type d");
		}
		// Exclude directory path from the result and provide list of relative
		// file path
		int startIndex = directoryAbsolutePath.length() + 1;
		// If last charactor of directoryAbsolutePath is not file separator
		// increase length by one
		startIndex = String
				.valueOf(
						directoryAbsolutePath.charAt(directoryAbsolutePath
								.length() - 1)).equals(File.separator) ? startIndex
				: 1 + startIndex;
		stringBuilder.append(" | cut -c " + startIndex + "-");
		
		//Sort
		//stringBuilder.append(" | sort ");
		
		if (include != null && StringUtils.isNotEmpty(include)) {
			stringBuilder.append(" | grep -E '" + include + "'");
		}
		if (exclude != null && StringUtils.isNotEmpty(exclude)) {
			stringBuilder.append(" | grep -vE '" + exclude + "'");
		}
		command = stringBuilder.toString();
		log.debug("Command to filter files {}", command);
		return command;
	}

	/**
	 * Returns hash of files listing of a directory that satisfies include and
	 * exclude criteria
	 * 
	 * @param imageId
	 *            image ID in process
	 * @param directoryMeasurement
	 * @param measurementType
	 * @return list of files separated by new line character
	 * @throws java.io.IOException
	 */
	public Digest getDirectoryHash(String imageId,
			DirectoryMeasurement directoryMeasurement, String measurementType)
			throws IOException {
		String fileList = getFiles(imageId, directoryMeasurement, true);
		Digest digest = Digest.algorithm(measurementType).digest(
				fileList.getBytes("UTF-8"));
		return digest;
	}

	public Digest getDirectoryHash(String imageId,
			DirectoryMeasurement directoryMeasurement, String measurementType, boolean skipDirectories)
			throws IOException {
		String fileList = getFiles(imageId, directoryMeasurement, skipDirectories);
		Digest digest = Digest.algorithm(measurementType).digest(
				fileList.getBytes("UTF-8"));
		return digest;
	}

	/**
	 * Returns hash of the file content. If file has a symbolic link it returns
	 * hash of a file pointed by symbolic link.
	 * 
	 * @param imageId
	 * @param fileMeasurement
	 * @param measurementType
	 * @return Digest of file is returned. If file is invalid, returns null.
	 * @throws IOException
	 */
	public Digest getFileHash(String imageId, FileMeasurement fileMeasurement,
			String measurementType) throws IOException {
		String filePath = DirectorUtil.getMountPath(imageId)+File.separator+"mount"
				+ fileMeasurement.getPath();
		filePath = getSymlinkValue(filePath);
		if (filePath == null || !new File(filePath).exists()){
			return null;
		}
		Digest digest = Digest.algorithm(measurementType).digest(
				FileUtils.readFileToByteArray(new File(filePath)));
		return digest;
	}

	/**
	 * Returns symbolic link of a file
	 * 
	 * @param filePath
	 * @return returns symbolic link path or null if there is circular symbolic
	 *         link
	 * @throws IOException
	 */
	public String getSymlinkValue(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		// symLinkSet is used to detect circular symbolic link.
		Set<String> symLinkSet = new HashSet<>();
		while (Files.isSymbolicLink(path)) {
			if (symLinkSet.contains(filePath)) {
				// Circular symbolic link detected
				return null;
			}
			symLinkSet.add(filePath);
			Path symLink = Files.readSymbolicLink(path);
			filePath = symLink.toString();
			if (filePath.startsWith(".") || filePath.startsWith("..")
					|| !filePath.startsWith(File.separator)) {
				StringBuilder sb = new StringBuilder();
				sb.append(path.toFile().getParent());
				sb.append(File.separator);
				sb.append(filePath);
				filePath = sb.toString();
			}
			filePath = new java.io.File(filePath).getCanonicalPath();
			if (!filePath.startsWith(DirectorUtil.getMountPath(imageId))) {
				log.info("Appending mount path for filepath = " + filePath);
				filePath = DirectorUtil.getMountPath(imageId) + File.separator
						+ "mount" + filePath;
			} else {
				log.info("NOT Appending mount path for filepath = " + filePath);
			}
			log.info("Symbolic link value for '" + path.toString() + "' is: '"
					+ filePath);
			path = Paths.get(filePath);
		}
		return filePath;
	}

	private String executeCommand(String command) throws ExecuteException, IOException{
		Result result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
		if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
			log.error(result.getStderr());
		}
		return result.getStdout();
	}
}
