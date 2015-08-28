/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.MountImageResponse;
import com.intel.director.common.MountVMImage;
import com.intel.director.service.ImageService;
import com.intel.director.util.DirectorUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author GS-0681
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MountVMImage.class, DirectorUtil.class})
public class ImageServiceIntegrationTest {

    static String createImageId = null;
    static ImageAttributes imageAttributes;
    static IPersistService dbService = new DbServiceImpl();
    static ImageService imageService = null;

    @AfterClass
    public static void tearDown() throws DbException {
        imageAttributes.id = createImageId;
        System.out.println("Deleting image :" + imageAttributes.id);
        dbService.destroyImage(imageAttributes);
    }

    @BeforeClass
    public static void setUpClass() {
        //Autowire
        ApplicationContext context = new ClassPathXmlApplicationContext("director-images-config.xml");
        imageService = (ImageService) context.getBean("imageServiceBean");

        System.out.println("Setting up image");
        imageAttributes = new ImageAttributes("soak", new Date(),
                "soak", new Date(), "IMG_001", "qcow",
                "VM", "ACTIVE", 512, 24, null, false, "C://temp");
        try {
            ImageAttributes saveImageMetadata = dbService.saveImageMetadata(imageAttributes);
            createImageId = saveImageMetadata.id;
            System.out.println("Created image :" + createImageId);

        } catch (DbException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testMountImage() throws Exception {
        PowerMockito.mockStatic(MountVMImage.class);
        String mountPath = "/mnt/vm/"+createImageId;
        Mockito.when(MountVMImage.mountImage(imageAttributes.location, mountPath)).thenReturn(1);
        Mockito.when(MountVMImage.unmountImage(mountPath)).thenReturn(1);
        MountImageResponse mountImage = imageService.mountImage(createImageId, "user1");
        Assert.assertEquals("Image mounted", "user1", mountImage.mounted_by_user_id);
    }

}
