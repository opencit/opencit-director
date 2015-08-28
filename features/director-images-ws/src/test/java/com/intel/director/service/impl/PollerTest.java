/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import org.apache.commons.configuration.BaseConfiguration;

/**
 *
 * @author GS-0681
 */
public class PollerTest {

    public static void main(String[] args) {
        org.apache.commons.configuration.Configuration apacheConfig = new BaseConfiguration();
        Configuration configuration = new CommonsConfiguration(apacheConfig);

    }
}
