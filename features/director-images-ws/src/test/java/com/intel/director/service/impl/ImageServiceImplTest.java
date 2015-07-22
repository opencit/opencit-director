/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.common.MountVMImage;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.persistence.ImagePersistenceManager;
import com.intel.director.service.ImageService;
import com.intel.director.util.DirectorUtil;
import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;

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
    ImagePersistenceManager imagePersistenceManager;
    String mountPath = "/mnt/director/images/678678-32131-grjeiog-321";

    @Before
    public void setup() throws NoSuchAlgorithmException {
        imagePersistenceManager = Mockito.mock(ImagePersistenceManager.class);
        imageService = new ImageServiceImpl(imagePersistenceManager);
        PowerMockito.mockStatic(MountVMImage.class);
        imageAttributes = new ImageAttributes();
        imageAttributes.id = imageId;
        imageAttributes.image_deployments = "VM";
        imageAttributes.image_format = "qcow2";
        imageAttributes.image_size = 1000L;
        imageAttributes.location = "/opt/director/vm/" + imageId;
        imageAttributes.mounted = false;
        imageAttributes.mountedBy = null;
        imageAttributes.name = "IMG_" + imageId;
        imageAttributes.status = null;
    }

    @Test
    public void testMountImageSuccess() throws Exception {
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(imageAttributes);
        Mockito.when(MountVMImage.mountImage(imageAttributes.location, mountPath)).thenReturn(1);
        MountImageResponse imageResponse = imageService.mountImage(imageId, user);
        Assert.assertEquals("Image mounteed", imageId, imageResponse.id);
    }

    @Test(expected = ImageMountException.class)
    public void testMountImageFailure() throws Exception {
        PowerMockito.mockStatic(DirectorUtil.class);
        Mockito.when(DirectorUtil.computeVMMountPath(imageAttributes.id)).thenReturn(mountPath);
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(imageAttributes);
        Mockito.when(MountVMImage.mountImage(imageAttributes.location, mountPath)).thenThrow(new RuntimeException("TEST"));
        imageService.mountImage(imageId, user);
    }

    @Test
    public void testUnMountImageSuccess() throws Exception {
        imageAttributes.mountedBy = "soak";
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(imageAttributes);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenReturn(1);
        UnmountImageResponse imageResponse = imageService.unMountImage(imageId, user);
        Assert.assertEquals("Image unmounted", imageId, imageResponse.id);
    }

    @Test(expected = ImageMountException.class)
    public void testUnMountImageByDifferentUser() throws Exception {
        imageAttributes.mountedBy = "soak1";
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(imageAttributes);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenReturn(1);
        imageService.unMountImage(imageId, user);
    }

    @Test(expected = ImageMountException.class)
    public void testUnMountImageFailure() throws Exception {
        imageAttributes.mountedBy = "soak";
        PowerMockito.mockStatic(DirectorUtil.class);
        Mockito.when(DirectorUtil.computeVMMountPath(imageAttributes.id)).thenReturn(mountPath);
        Mockito.when(imagePersistenceManager.fetchImageById(imageId)).thenReturn(imageAttributes);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenThrow(new RuntimeException("TEST"));
        imageService.unMountImage(imageId, user);
    }

    @Test
    public void testUploadImageMetaDataToTrustDirector() {
        Assert.assertTrue(true);
    }

    @Test
    public void testUploadImageToTrustDirector() {
        Assert.assertTrue(true);
    }

    @Test
    public void testSearchImages() {
        Assert.assertTrue(true);
    }

    @Test
    public void testSearchFilesInImage() {
        Assert.assertTrue(true);
    }

    @Test
    public void testUploadImageToImageStore() {
        Assert.assertTrue(true);
    }
}
