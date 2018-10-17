package com.intel.director.common;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

public class FileUtilityOperation {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileUtilityOperation.class);

    // Extract the tar.gz file
    public boolean extractCompressedImage(String tarFileLocation, String destPath) {
	Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
	File sourceFile = new File(tarFileLocation);
	File destDir = new File(destPath);

	try {
	    if (destDir.exists()) {
		deleteFileOrDirectory(destDir);
	    }
	    archiver.extract(sourceFile, destDir);
	} catch (IOException ex) {
	    log.error("Error in extracting image ", ex);
	    return false;
	}
	return true;
    }

    // Delete the directory with its contents
    public void deleteFileOrDirectory(File file) {
	if (file == null) {
	    return;
	} else if (file.isDirectory()) {
	    String[] fileList = file.list();

	    // directory is empty, then delete it
	    if (fileList == null || fileList.length == 0) {
		file.delete();
	    } else {
		// list all the directory contents
		if (fileList == null || fileList.length == 0) {
		    return;
		}
		for (String temp : fileList) {
		    File fileDelete = new File(file, temp);
		    deleteFileOrDirectory(fileDelete);
		}
		// check the directory again, if empty then delete it
		fileList = file.list();

		if (fileList == null || fileList.length == 0) {
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
		log.error("Unable to close streams used for writing manifest and policy", e);
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
		if (writer != null) {
		    writer.close();
		}
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		log.error("Error in closing writer in writeToFile()", e1);
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
	final String PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
		+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
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
	    if (matcher.matches() && (Integer.parseInt(port) > 0 && Integer.parseInt(port) < 65536)) {
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

    public int createTar(String tarFilePath, List<String> filePaths) {

	boolean proceed = true;
        
	try(FileOutputStream dest = new FileOutputStream(tarFilePath)){
	

	// Create a TarOutputStream
	TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

	// Files to tar
	File[] filesToTar = new File[filePaths.size()];
	int i = 0;
	for (String filePath : filePaths) {
	    filesToTar[i++] = new File(filePath);
	}

	for (File f : filesToTar) {
	    try {
		out.putNextEntry(new TarEntry(f, f.getName()));
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    BufferedInputStream origin;
	    try {
		origin = new BufferedInputStream(new FileInputStream(f));
	    } catch (FileNotFoundException e) {
		log.error("Error reading source file {}", f.getAbsolutePath(), e);
		return 1;
	    }
	    int count;
	    byte data[] = new byte[2048];

	    try {
		while ((count = origin.read(data)) != -1) {
		    out.write(data, 0, count);
		}
	    } catch (IOException e) {
		log.error("Error reding content from origin file", e);
		closeOriginStream(origin);
		return 1;
	    }

	    try {
		out.flush();
	    } catch (IOException e) {
		log.error("Error flushing TAR stream", e);
		closeOriginStream(origin);
		return 1;
	    }
	    if (closeOriginStream(origin) == 1) {
		return 1;
	    }

	}

	try {
	    out.close();
	} catch (IOException e) {
	    log.error("Error closing TAR stream", e);
	    return 1;
	}
        
    } catch (IOException e) {
	log.error("Error creating tar file", e);
	proceed = false;
    } 
    if (!proceed) {
        return 1;
    }
    return 0;

    }

    private int closeOriginStream(BufferedInputStream origin) {
	try {
	    origin.close();
	} catch (IOException e) {
	    log.error("Error flushing origin file stream", e);
	    return 1;
	}
	return 0;
    }
}
