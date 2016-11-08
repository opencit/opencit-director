package com.intel.director.service.impl;

import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.upload.Chunk;
import com.intel.director.api.upload.FileInformation;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ImageService;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upload Service to handle chunk processing
 */
public class UploadService {

  private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);

  private static Map<String, FileInformation> uploads = new ConcurrentHashMap<>();
  private String baseDirectory = "upload";

  private static Lock uploadLock = new ReentrantLock();

  public UploadService() {

  }

  public UploadService(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public boolean isChunkUploaded(Chunk chunk) {
    return uploads.containsKey(chunk.getResumableIdentifier()) &&
        uploads.get(chunk.getResumableIdentifier())
            .getChunks()
            .contains(chunk.getResumableChunkNumber());
  }

  /**
   * Write chunk to file.
   * @param chunk Chunk information
   * @param data File data
   * @return true if File is completed else false
   */
  public boolean uploadChunk(String imageId, Chunk chunk, byte[] data) throws DirectorException {
    FileInformation info = fromChunk(chunk);
    try {
      RandomAccessFile raf = new RandomAccessFile(info.getFilePath(), "rw");
      raf.seek((chunk.getResumableChunkNumber() - 1) * info.getChunkSize());
      raf.write(data);
      raf.close();
    } catch (IOException e) {
      LOG.error("Error uploading chunk", e);
      return false;
    }

    info.getChunks().add(chunk.getResumableChunkNumber());
    if (info.uploadFinished()) {
      ImageService imageService = new ImageServiceImpl();
      ImageInfo imageInfo = imageService.fetchImageById(imageId);
      File uploadedFile = new File(info.getFilePath());
      uploadedFile.renameTo(Paths.get(imageInfo.getLocation(),imageInfo.getImage_name()).toFile());
      imageInfo.setStatus(Constants.COMPLETE);
      imageInfo.setDeleted(false);
      imageService.updateImageMetadata(imageInfo);
      uploads.remove(info.getIdentifier());
      return true;
    } else {
      return false;
    }
  }

  private FileInformation fromChunk(Chunk chunk) {
    FileInformation fileInformation;
    try {
      uploadLock.lock();
      if (uploads.containsKey(chunk.getResumableIdentifier())) {
        fileInformation = uploads.get(chunk.getResumableIdentifier());
        return fileInformation;
      } else {
        fileInformation = new FileInformation();
        fileInformation.setFileName(chunk.getResumableFilename());
        fileInformation.setRelativePath(chunk.getResumableRelativePath());
        fileInformation.setIdentifier(chunk.getResumableIdentifier());
        fileInformation.setChunkSize(chunk.getResumableChunkSize());
        fileInformation.setTotalSize(chunk.getResumableTotalSize());
        String filePath = new File(baseDirectory, chunk.getResumableFilename()).getAbsolutePath();
        fileInformation.setFilePath(filePath);
        uploads.put(chunk.getResumableIdentifier(), fileInformation);
        return fileInformation;
      }
    } finally {
      uploadLock.unlock();
    }
  }
}
