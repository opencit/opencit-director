package com.intel.director.api.upload;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * File information. Used to store and track information of uploading file.
 */
public class FileInformation {
    private long chunkSize;
    private long totalSize;
    private String identifier;
    private String fileName;
    private String relativePath;
    private String filePath;
    private Set<Long> chunks = new HashSet<>();

    public boolean isValid() {
        if (chunkSize < 0 || totalSize < 0
                || StringUtils.isEmpty(identifier)
                || StringUtils.isEmpty(fileName)
                || StringUtils.isEmpty(relativePath)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean uploadFinished() {
        long count = (long) Math.ceil(((double) totalSize) / ((double) chunkSize));
        for (long i = 1; i < count ; i++) {
            if (!chunks.contains(i)) {
                return false;
            }
        }
        return true;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Set<Long> getChunks() {
        return chunks;
    }

}
