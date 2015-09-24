/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.director.service.ImageService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author GS-0681
 */
public class AutowireTest {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("director-images-config.xml");
       
   ///     imageService.pleaseAutoWire();

    }
}
