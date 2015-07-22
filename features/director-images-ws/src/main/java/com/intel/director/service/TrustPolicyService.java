package com.intel.director.service;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * this interface defines the actions associated with the trust policy
 * Example: Sign a trust policy
 * 
 * @author GS-0681
 */
public interface TrustPolicyService {
    public void createTrustPolicy(String polictyXml);
    public void signTrustPolicy();
    
}
