/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ImageDeploymentsResponse;
import com.intel.director.api.ImageFormatsResponse;
import com.intel.director.api.ImageLaunchPolicy;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.api.ListImageLaunchPolicyResponse;
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
    	ImageDeploymentsResponse imageDeployment = new ImageDeploymentsResponse();
    	imageDeployment.setName("VM");
    	imageDeployment.setDisplay_name("Virtualized Server");
    	deploymentsResponse.image_deployments.add(imageDeployment);
        return deploymentsResponse;
    }

    @Override
    public ListImageFormatsResponse getImageFormats() {
        ListImageFormatsResponse formatsResponse = new ListImageFormatsResponse();
        ImageFormatsResponse imageFormatResponse = new ImageFormatsResponse();
        imageFormatResponse.setName("qcow2");
        imageFormatResponse.setDisplay_name("qcow2");
        formatsResponse.image_formats.add(imageFormatResponse);
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
    
    @Override
    public ListImageLaunchPolicyResponse getImageLaunchPolicies(String deployment_type) {
        ListImageLaunchPolicyResponse imageLaunchPoliciesResponse = new ListImageLaunchPolicyResponse();
        imageLaunchPoliciesResponse.image_launch_policies = new ArrayList<ImageLaunchPolicy>();
        ImageLaunchPolicy imagelaunchpolicy = new ImageLaunchPolicy();
        imagelaunchpolicy.image_deployments = new ArrayList<String>();
        imagelaunchpolicy.name = Constants.LAUNCH_CONTROL_POLICY_HASH_ONLY;
        imagelaunchpolicy.value = "Hash Only";
        imagelaunchpolicy.image_deployments.add(Constants.DEPLOYMENT_TYPE_VM);
        imagelaunchpolicy.image_deployments.add(Constants.DEPLOYMENT_TYPE_BAREMETAL);
        imageLaunchPoliciesResponse.image_launch_policies.add(imagelaunchpolicy);
        imagelaunchpolicy = new ImageLaunchPolicy();
        imagelaunchpolicy.image_deployments = new ArrayList<String>();
        imagelaunchpolicy.name = Constants.LAUNCH_CONTROL_POLICY_HASH_AND_ENFORCE;
        imagelaunchpolicy.value = "Hash and enforce";
        imagelaunchpolicy.image_deployments.add(Constants.DEPLOYMENT_TYPE_VM);
        imageLaunchPoliciesResponse.image_launch_policies.add(imagelaunchpolicy);
        imagelaunchpolicy = new ImageLaunchPolicy();
        imagelaunchpolicy.image_deployments = new ArrayList<String>();
        imagelaunchpolicy.name = "encrypted";
        imagelaunchpolicy.value = "Encryption";
        imagelaunchpolicy.image_deployments.add(Constants.DEPLOYMENT_TYPE_VM);
        imageLaunchPoliciesResponse.image_launch_policies.add(imagelaunchpolicy);
        if(StringUtils.isEmpty(deployment_type)){
        	return imageLaunchPoliciesResponse;
        }
        ListImageLaunchPolicyResponse result = new ListImageLaunchPolicyResponse();
        result.image_launch_policies = new ArrayList<ImageLaunchPolicy>();
        for(ImageLaunchPolicy image : imageLaunchPoliciesResponse.image_launch_policies){
        	if(image.image_deployments.contains(deployment_type)){
        		result.image_launch_policies.add(image);
        	}
        }
        return result;
        
    }

}
