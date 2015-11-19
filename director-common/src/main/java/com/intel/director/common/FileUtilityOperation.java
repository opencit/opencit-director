package com.intel.director.common;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

public class FileUtilityOperation {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(FileUtilityOperation.class);

	// Extract the tar.gz file
	public boolean extractCompressedImage(String tarFileLocation,
			String destPath) {
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR,
				CompressionType.GZIP);
		File sourceFile = new File(tarFileLocation);
		File destDir = new File(destPath);

		try {
			if (destDir.exists()) {
				deleteDir(destDir);
			}
			archiver.extract(sourceFile, destDir);
		} catch (IOException ex) {
			log.error("Error in extracting image ", ex);
			return false;
		}
		return true;
	}

	// Delete the directory with its contents
	public void deleteDir(File file) {
		if (file == null) {
			return;
		}
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list() == null || file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					deleteDir(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list() == null || file.list().length == 0) {
					file.delete();
					log.info("Directory is deleted : " + file.getAbsolutePath());
				}
			}
		} else {
			file.delete();
		}

	}

	public boolean writeToFile(String path, String value) {
		boolean status = true;
		FileWriter fw = null;
		BufferedWriter bw = null;

		// Write policy
		try {
			File f = new File(path);
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			bw.write(value);
		} catch (IOException e) {
			log.error("Error writing to file " + path, e);
			status = false;
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				log.error(
						"Unable to close streams used for writing manifest and policy",
						e);
			}

		}
		return status;

	}

	public void writeToFile(File file, String value, boolean append) {
		// File passFile = new File("/tmp/vmpass.txt");
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, append);

			BufferedWriter bufferWriter = new BufferedWriter(writer);
			bufferWriter.write(value);
			bufferWriter.newLine();
			bufferWriter.close();
			writer.close();
			file.setExecutable(true);
		} catch (IOException e) {
			// TODO Handle Error
			try {
				if(writer != null)
				{
					writer.close();
				}	
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				log.error("Error in closing writer in writeToFile()",e1);
			}
			log.error("Error in writing propeties to files");
		}
	}

	// Encode with Base64
	public String base64Encode(String value) {
		byte[] result = Base64.encodeBase64(value.getBytes());
		return new String(result);
	}

	// Validate the UUID
	public boolean validateUUID(String uuid) {
		try {
			UUID uuidObj = UUID.fromString(uuid);
			return uuidObj.toString().equals(uuid);
		} catch (Exception e) {
			return false;
		}

	}

	// Validate the IP address
	public boolean validateIPAddress(String ip) {
		final String PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		Pattern pattern = Pattern.compile(PATTERN);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	// Validate the Port
	public boolean validatePort(String port) {
		final String PATTERN = "^[0-9]+$";
		Pattern pattern = Pattern.compile(PATTERN);
		Matcher matcher = pattern.matcher(port);
		try {
			if (matcher.matches()
					&& (Integer.parseInt(port) > 0 && Integer.parseInt(port) < 65536)) {
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException ex) {
			log.error(null, ex);
			return false;
		}
	}

	public boolean createNewFile(String path) {
		boolean ret = true;
		File f = new File(path);
		if (!f.exists()) {
			try {
				ret = f.createNewFile();
			} catch (IOException e) {
				log.error("Error creating new file at path " + path, e);
				ret = false;
			}
		}
		return ret;
	}
}
