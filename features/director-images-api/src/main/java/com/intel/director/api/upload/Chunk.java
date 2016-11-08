package com.intel.director.api.upload;

import javax.ws.rs.QueryParam;
/**
 * File Chunk Information for uploading chunk.
 * This does not represent the actual chunk data, but information/metadata of particular chunk
 * These parameters helps client and server to determine how many file chunks are uploaded and how many are remaining
 */
public class Chunk {
    @QueryParam("resumableChunkSize")
    private long resumableChunkSize;

    @QueryParam("resumableTotalSize")
    private long resumableTotalSize;

    @QueryParam("resumableChunkNumber")
    private long resumableChunkNumber;

    @QueryParam("resumableIdentifier")
    private String resumableIdentifier;

    @QueryParam("resumableCurrentChunkSize")
    private long resumableCurrentChunkSize;

    @QueryParam("resumableFilename")
    private String resumableFilename;

    @QueryParam("resumableRelativePath")
    private String resumableRelativePath;


    public long getResumableChunkSize() {
        return resumableChunkSize;
    }

    public void setResumableChunkSize(long resumableChunkSize) {
        this.resumableChunkSize = resumableChunkSize;
    }

    public long getResumableTotalSize() {
        return resumableTotalSize;
    }

    public void setResumableTotalSize(long resumableTotalSize) {
        this.resumableTotalSize = resumableTotalSize;
    }

    public long getResumableChunkNumber() {
        return resumableChunkNumber;
    }

    public void setResumableChunkNumber(long resumableChunkNumber) {
        this.resumableChunkNumber = resumableChunkNumber;
    }

    public String getResumableIdentifier() {
        return resumableIdentifier;
    }

    public void setResumableIdentifier(String resumableIdentifier) {
        this.resumableIdentifier = resumableIdentifier;
    }

    public long getResumableCurrentChunkSize() {
        return resumableCurrentChunkSize;
    }

    public void setResumableCurrentChunkSize(long resumableCurrentChunkSize) {
        this.resumableCurrentChunkSize = resumableCurrentChunkSize;
    }

    public String getResumableFilename() {
        return resumableFilename;
    }

    public void setResumableFilename(String resumableFilename) {
        this.resumableFilename = resumableFilename;
    }

    public String getResumableRelativePath() {
        return resumableRelativePath;
    }

    public void setResumableRelativePath(String resumableRelativePath) {
        this.resumableRelativePath = resumableRelativePath;
    }

    @Override
    public String toString() {
        return String.format("ResumableChunkSize: %d, ResumableTotalSize: %d, ResumableChunkNumber: %d" +
                ",  ResumableIdentifier: %s, ResumableCurrentChunkSize: %d, " +
                "ResumableFilename: %s, ResumableRelativePath: %s", resumableChunkSize, resumableTotalSize,
                resumableChunkNumber, resumableIdentifier, resumableCurrentChunkSize,
                resumableFilename, resumableRelativePath);
    }
}
