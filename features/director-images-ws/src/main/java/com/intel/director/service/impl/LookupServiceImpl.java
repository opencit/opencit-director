/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.service.LookupService;
import java.util.ArrayList;

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
        imageLaunchPoliciesResponse.image_launch_policies.add("MeasureOnly");
        imageLaunchPoliciesResponse.image_launch_policies.add("MeasureAndEnforce");
        return imageLaunchPoliciesResponse;
    }

}
