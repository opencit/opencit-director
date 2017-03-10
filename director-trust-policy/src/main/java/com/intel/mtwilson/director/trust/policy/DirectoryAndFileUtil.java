/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.trust.policy;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.director.common.DirectorUtil;
import com.intel.mtwilson.trustpolicy2.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy2.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy2.xml.SymlinkMeasurement;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author boskisha
 */
public class DirectoryAndFileUtil {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(DirectoryAndFileUtil.class);

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

  public String getFilesAndDirectories(String imageId, DirectoryMeasurement dirMeasurement,
      boolean addMountPath)
      throws FileNotFoundException, IOException {
    // Create find command
    String command = createFindCommand(imageId, dirMeasurement, false, addMountPath);
    // Execute find command and return result
    return executeCommand(command);
  }

  public String getFiles(String imageId, DirectoryMeasurement dirMeasurement)
      throws FileNotFoundException, IOException {
    // Create find command
    String command = createFindCommand(imageId, dirMeasurement, true, true);
    // Execute find command and return result
    return executeCommand(command);
  }

  /**
   * This method applies include and exclude criteria on a directory and
   * returns list of files that satisfies criteria
   *
   * @param imageId image ID in process
   * @return list of files separated by new line character
   * @throws FileNotFoundException if directory path is not valid it throws exception
   */
  public String getFiles(String imageId, DirectoryMeasurement dirMeasurement,
      boolean skipDirectories)
      throws FileNotFoundException, IOException {
    // Create find command
    String command = createFindCommand(imageId, dirMeasurement, skipDirectories, true);
    // Execute find command and return result
    return executeCommand(command);
  }

  /**
   * creates linux find command to filter files using include and exclude
   * regular expression
   *
   * @param imageId Image ID
   * @param dirMeasurement Directory measurement object containing measurement related parameters
   * like filtertype, include regex, exclude regex
   * @param skipDirectories Flag to skip directories from Find command
   * @return linux find command
   */
  private String createFindCommand(String imageId, DirectoryMeasurement dirMeasurement,
      boolean skipDirectories, boolean addMountPath) {
    String pathOfDir = dirMeasurement.getPath();
    if (!pathOfDir.endsWith(File.separator)) {
      pathOfDir += File.separator;
    }
    String directoryAbsolutePath = null;
    if (addMountPath) {
      directoryAbsolutePath =
          DirectorUtil.getMountPath(imageId) + File.separator + "mount" + pathOfDir;
    } else {
      directoryAbsolutePath = pathOfDir;
    }
    String include = dirMeasurement.getInclude();
    String exclude = dirMeasurement.getExclude();
    if ("wildcard".equalsIgnoreCase(dirMeasurement.getFilterType())) {
      include = convertWildCardToRegex(dirMeasurement.getInclude());
      exclude = convertWildCardToRegex(dirMeasurement.getExclude());
      log.debug("Wildcard to Regex : {}, {}", dirMeasurement.getInclude(), include);
    }
    StringBuilder stringBuilder = new StringBuilder("find -P " + directoryAbsolutePath);

    //if (dirMeasurement.isRecursive() == false) {
    stringBuilder.append("  -maxdepth 1");
    //}
    if (skipDirectories) {
      stringBuilder.append(" ! -type d");
    }
    // Exclude directory path from the result and provide list of relative
    // file path
    int startIndex = directoryAbsolutePath.length() + 1;
    // If last charactor of directoryAbsolutePath is not file separator
    // increase length by one
    startIndex = String.valueOf(directoryAbsolutePath.charAt(directoryAbsolutePath.length() - 1))
        .equals(File.separator) ? startIndex : 1 + startIndex;
    stringBuilder.append(" | cut -c " + startIndex + "-");

    // Sort
    stringBuilder.append(" | sort ");

    if (StringUtils.isNotEmpty(include)) {
      stringBuilder.append(" | grep -E '" + include + "'");
    }
    if (StringUtils.isNotEmpty(exclude)) {
      stringBuilder.append(" | grep -vE '" + exclude + "'");
    }
    String command = stringBuilder.toString();
    log.debug("Command to filter files {}", command);
    return command;
  }

  /**
   * Returns hash of files listing of a directory that satisfies include and
   * exclude criteria
   *
   * @param imageId image ID in process
   * @return list of files separated by new line character
   * @throws java.io.IOException
   */
  public Digest getDirectoryHash(String imageId, DirectoryMeasurement directoryMeasurement,
      String measurementType)
      throws IOException {
    String fileList = getFiles(imageId, directoryMeasurement, true);
    Digest digest = Digest.algorithm(measurementType).digest(fileList.getBytes("UTF-8"));
    return digest;
  }

  public Digest getDirectoryHash(String imageId, DirectoryMeasurement directoryMeasurement,
      String measurementType,
      boolean skipDirectories) throws IOException {
    String fileList = getFiles(imageId, directoryMeasurement, skipDirectories);
    Digest digest = Digest.algorithm(measurementType).digest(fileList.getBytes("UTF-8"));
    return digest;
  }

  /**
   * Get Symlink hash.
   * The hash is calculated using Symlink path + Target path
   * @param imageId
   * @param symlinkMeasurement
   * @param measurementType
   * @return
   * @throws IOException
   */
  public Digest getSymlinkHash(String imageId, SymlinkMeasurement symlinkMeasurement,
      String measurementType)
      throws IOException {
    log.debug("Symlink path:{}", symlinkMeasurement.getPath());
    String filePath = DirectorUtil.getMountPath(imageId) + File.separator +  "mount" + symlinkMeasurement.getPath();
    log.debug("Symlink mount path:{}", filePath);
    String symlinkTarget = Files.readSymbolicLink(Paths.get(filePath)).toString();
    log.debug("Symlink target:{}", symlinkTarget);
    String mergedLinkTarget = symlinkMeasurement.getPath() +
        symlinkTarget.replace(DirectorUtil.getMountPath(imageId) + File.separator +  "mount", "");
    log.debug("Merged Symlink Target: {}", mergedLinkTarget);
    Digest digest = Digest.algorithm(measurementType).digest(
        mergedLinkTarget.getBytes(StandardCharsets.UTF_8));
    log.debug("Symlink digest: {}", digest.toHex());
    return digest;
  }

  /**
   * Returns hash of the file content. If file has a symbolic link it returns
   * hash of a file pointed by symbolic link.
   *
   * @return Digest of file is returned. If file is invalid, returns null.
   * @throws IOException
   */
  public Digest getFileHash(String imageId, FileMeasurement fileMeasurement, String measurementType)
      throws IOException {
    String filePath =
        DirectorUtil.getMountPath(imageId) + File.separator + "mount" + fileMeasurement.getPath();
    filePath = getSymlinkValue(filePath);
    if (filePath == null || !new File(filePath).exists()) {
      return null;
    }
    Digest digest =
        Digest.algorithm(measurementType).digest(FileUtils.readFileToByteArray(new File(filePath)));
    return digest;
  }

  /**
   * Returns symbolic link of a file
   *
   * @return returns symbolic link path or null if there is circular symbolic link
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
      if (filePath.startsWith(".") || filePath.startsWith("..") || !filePath.startsWith(
          File.separator)) {
        StringBuilder sb = new StringBuilder();
        sb.append(path.toFile().getParent());
        sb.append(File.separator);
        sb.append(filePath);
        filePath = sb.toString();
      }
      filePath = new java.io.File(filePath).getCanonicalPath();
      StringBuffer stringBuffer = new StringBuffer();

      if (!filePath.startsWith(DirectorUtil.getMountPath(imageId))) {
        log.debug("Appending mount path for filepath = {}", filePath);
        filePath = stringBuffer.append(DirectorUtil.getMountPath(imageId)).append(File.separator)
            .append("mount").append(filePath).toString();
      } else {
        log.debug("NOT Appending mount path for filepath = {}", filePath);
      }
      log.debug("Symbolic link value for '{}' is: '{}'", path.toString(), filePath);
      path = Paths.get(filePath);
    }
    return filePath;
  }

  public String getSymlinkTarget(String filePath, String mountPath) {
    Path path = Paths.get(filePath);
    String target = null;
    try {
      if (Files.isSymbolicLink(path) && Files.exists(path)) {
        Path symLink = path.toRealPath();
        target = symLink.toString();
        if (target.startsWith(mountPath)) {
          target = target.replace(mountPath, "");
        }
      } else {
        log.info("Symbolic Link target does not exists: {}", path);
        target = "";
      }
    } catch (IOException e) {
      log.error("Error in getting symbolic link target", path, e);
    }
    return target;
  }

  private String executeCommand(String command) throws IOException {
    Result result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
    if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
      log.error(result.getStderr());
    }
    return result.getStdout();
  }

  /**
   * Convert wildcard to regex expression so Grep command can use it for identification
   * It does this by going through string and
   * replace/escape special characters related to regex expression.
   * e.g. '*' is converted into '.*' , '?' converted into '.'
   * @param wildcard - Wildcard string
   * @return - Regex string
   */
  private static String convertWildCardToRegex(String wildcard) {
    if (StringUtils.isEmpty(wildcard)) {
      return "";
    }
    StringBuilder s = new StringBuilder(wildcard.length());
    s.append('^');
    for (int i = 0, is = wildcard.length(); i < is; i++) {
      char c = wildcard.charAt(i);
      switch (c) {
        case '*':
          s.append(".*");
          break;
        case '?':
          s.append(".");
          break;
        case '(':
        case ')':
        case '[':
        case ']':
        case '$':
        case '^':
        case '.':
        case '{':
        case '}':
        case '|':
        case '\\':
          s.append("\\");
          s.append(c);
          break;
        default:
          s.append(c);
          break;
      }
    }
    s.append('$');
    return (s.toString());
  }
}
