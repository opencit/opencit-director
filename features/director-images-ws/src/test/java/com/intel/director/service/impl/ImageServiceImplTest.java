/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.TrustDirectorImageUploadRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.MountVMImage;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.service.ImageService;
import com.intel.director.util.DirectorUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.IPersistService;

/**
 *
 * @author GS-0681
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MountVMImage.class, DirectorUtil.class})

public class ImageServiceImplTest {

    ImageService imageService;
  
    String imageId = "123";
    String user = "soak";
    MountVMImage mountVMImageService;
    ImageAttributes imageAttributes;
    IPersistService imagePersistenceManager;
    String mountPath = "/mnt/director/images/678678-32131-grjeiog-321";
    ImageInfo info ;
   //// @Before
    public void setup() throws NoSuchAlgorithmException {
        imagePersistenceManager = Mockito.mock(IPersistService.class);
    
        imageService = new ImageServiceImpl(imagePersistenceManager);
        PowerMockito.mockStatic(MountVMImage.class);
        imageAttributes = new ImageInfo();
        imageAttributes.id = imageId;
        imageAttributes.image_deployments = "VM";
        imageAttributes.image_format = "qcow2";
        imageAttributes.image_size = 1000;
        imageAttributes.location = "/opt/director/vm/" + imageId;
        imageAttributes.mounted_by_user_id = null;
        imageAttributes.name = "IMG_" + imageId;
        imageAttributes.status = null;
        
        
        info = new ImageInfo();
        info = (ImageInfo) imageAttributes;
    }

   /// @Test
    public void testMountImageSuccess() throws Exception {
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(info);
        Mockito.when(MountVMImage.mountImage(imageAttributes.location, mountPath)).thenReturn(1);
        MountImageResponse imageResponse = imageService.mountImage(imageId, user);
        Assert.assertEquals("Image mounteed", imageId, imageResponse.id);
    }

    ///@Test(expected = ImageMountException.class)
    public void testMountImageFailure() throws Exception {
        PowerMockito.mockStatic(DirectorUtil.class);
        Mockito.when(DirectorUtil.computeVMMountPath(imageAttributes.id)).thenReturn(mountPath);
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(info);
        Mockito.when(MountVMImage.mountImage(imageAttributes.location, mountPath)).thenThrow(new RuntimeException("TEST"));
        imageService.mountImage(imageId, user);
    }

   /// @Test
    public void testMountImageAlreadyMounted() throws Exception {
        imageAttributes.mounted_by_user_id = "soak_1";
        PowerMockito.mockStatic(DirectorUtil.class);
        Mockito.when(DirectorUtil.computeVMMountPath(imageAttributes.id)).thenReturn(mountPath);
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(info);
        Mockito.when(MountVMImage.mountImage(imageAttributes.location, mountPath)).thenThrow(new RuntimeException("TEST"));
        String msg = null;
        try {
            imageService.mountImage(imageId, user);
        } catch (ImageMountException exception) {
            msg = exception.getMessage();
        }
        Assert.assertEquals("Expected error", "Unable to mount image. Image is already in use by user: soak_1", msg);
    }

    ///@Test
    public void testUnMountImageSuccess() throws Exception {
        imageAttributes.mounted_by_user_id = "soak";
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(info);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenReturn(1);
        UnmountImageResponse imageResponse = imageService.unMountImage(imageId, user);
        Assert.assertEquals("Image unmounted", imageId, imageResponse.id);
    }

   /// @Test(expected = ImageMountException.class)
    public void testUnMountImageByDifferentUser() throws Exception {
        imageAttributes.mounted_by_user_id = "soak1";
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(info);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenReturn(1);
        imageService.unMountImage(imageId, user);
    }

   /// @Test(expected = ImageMountException.class)
    public void testUnMountImageFailure() throws Exception {
        imageAttributes.mounted_by_user_id = "soak";
        PowerMockito.mockStatic(DirectorUtil.class);
        Mockito.when(DirectorUtil.computeVMMountPath(imageAttributes.id)).thenReturn(mountPath);
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(info);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenThrow(new RuntimeException("TEST"));
        imageService.unMountImage(imageId, user);
    }

  ///  @Test
    public void testSearchImages() {
        Assert.assertTrue(true);
    }

  ///  @Test
    public void testSearchFilesInImage() {
        Assert.assertTrue(true);
    }
    
    

}
