/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import java.util.ArrayList;

import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.api.ui.ImageLaunchPolicyKeyValue;
import com.intel.director.common.Constants;
import com.intel.director.service.LookupService;

/**
 *
 * @author Siddharth
 */
public class LookupServiceImpl implements LookupService {

    @Override
    public ListImageDeploymentsResponse getImageDeployments() {
        ListImageDeploymentsResponse deploymentsResponse = new ListImageDeploymentsResponse();
        deploymentsResponse.image_deployments = new ArrayList<>();
        deploymentsResponse.image_deployments.add("VM");
        deploymentsResponse.image_deployments.add("Bare_Metal");
        return deploymentsResponse;
    }

    @Override
    public ListImageFormatsResponse getImageFormats() {
        ListImageFormatsResponse formatsResponse = new ListImageFormatsResponse();
        formatsResponse.image_formats = new ArrayList<>();
        formatsResponse.image_formats.add("qcow2");
        formatsResponse.image_formats.add("vhd");
        return formatsResponse;
    }

    @Override
    public ListImageLaunchPoliciesResponse getImageLaunchPolicies() {
        ListImageLaunchPoliciesResponse imageLaunchPoliciesResponse = new ListImageLaunchPoliciesResponse();
        imageLaunchPoliciesResponse.image_launch_policies = new ArrayList<>();
        ImageLaunchPolicyKeyValue imagelaunchpolicykeyvalue = new ImageLaunchPolicyKeyValue();
        imagelaunchpolicykeyvalue.setKey(Constants.LAUNCH_CONTROL_POLICY_HASH_ONLY);
        imagelaunchpolicykeyvalue.setValue("Hash Only");
        imageLaunchPoliciesResponse.image_launch_policies.add(imagelaunchpolicykeyvalue);
        imagelaunchpolicykeyvalue = new ImageLaunchPolicyKeyValue();
        imagelaunchpolicykeyvalue.setKey(Constants.LAUNCH_CONTROL_POLICY_HASH_AND_ENFORCE	);
        imagelaunchpolicykeyvalue.setValue("Hash and enforce");
        imageLaunchPoliciesResponse.image_launch_policies.add(imagelaunchpolicykeyvalue);
        return imageLaunchPoliciesResponse;
    }

}
