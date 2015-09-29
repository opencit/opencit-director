/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.director.service.impl;

import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.service.LookupService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LookupServiceImplTest {

    LookupService lookupService = null;

   /// @Before
    public void setup() {
        lookupService = new LookupServiceImpl();
    }

   /// @Test
    public void testGetImageDeployments() {
        ListImageDeploymentsResponse imageDeployments = lookupService.getImageDeployments();
        Assert.assertEquals("2 deployment types", 2, imageDeployments.image_deployments.size());
    }

   /// @Test
    public void testGetImageFormats() {
        ListImageFormatsResponse imageFormats = lookupService.getImageFormats();
        Assert.assertEquals("2 format types", 2, imageFormats.image_formats.size());
    }

   /// @Test
    public void testGetLaunchPolicies() {
        ListImageLaunchPoliciesResponse imageLaunchPolicies = lookupService.getImageLaunchPolicies();
        Assert.assertEquals("2 launch policies", 2, imageLaunchPolicies.image_launch_policies.size());
    }
}
