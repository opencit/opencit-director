package com.intel.director.service;

import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This interface has methods to get the constants required by the application
 * Depending on the implementation, it can go to the DB, read from the file
 * system or get from memory
 *
 * @author GS-0681
 */
public interface LookupService {

    public ListImageDeploymentsResponse getImageDeployments();

    public ListImageFormatsResponse getImageFormats();

    public ListImageLaunchPoliciesResponse getImageLaunchPolicies();

}
