package com.intel.director.service;

import java.util.List;

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.common.exception.DirectorException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * this interface defines the actions associated with the trust policy Example:
 * Sign a trust policy
 *
 * @author GS-0681
 */
public interface TrustPolicyService {

    public void createTrustPolicy(String polictyXml);

    public String signTrustPolicy(String policyXml) throws DirectorException;
    
    public TrustPolicy archiveAndSaveTrustPolicy(String policyXml) throws DirectorException;
    
    public TrustPolicy archiveAndSaveTrustPolicy(String policyXml,String userName) throws DirectorException;
    
    public void copyTrustPolicyAndManifestToHost(String policyXml) throws DirectorException;
    
    public void addEncryption(com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy) throws DirectorException;

    public void calculateHashes(com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy) throws DirectorException;
    
	public TrustPolicyDraft editTrustPolicyDraft(
			TrustPolicyDraftEditRequest trustpolicyDraftEditRequest)
			throws DirectorException ;
	
	public void deleteTrustPolicy(String trust_policy_id)
			throws DirectorException ;
	public String getTrustPolicyForImage(String imageId);

	public CreateTrustPolicyMetaDataResponse saveTrustPolicyMetaData(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws DirectorException;
	

	public CreateTrustPolicyMetaDataResponse getPolicyMetadata(String draftid)
			throws DirectorException;


	public CreateTrustPolicyMetaDataResponse getPolicyMetadataForImage(
			String image_id) throws DirectorException;
	
	
	public TrustPolicyDraft createPolicyDraftFromPolicy(String imageId)
			throws DirectorException;

	public TrustPolicy getTrustPolicyByTrustId(String trustId);

	public TrustPolicy getTrustPolicyByImageId(String imageId)
			throws DirectorException;

	void deleteTrustPolicyDraft(String trust_policy_draft_id)
			throws DirectorException;

	public TrustPolicyResponse getTrustPolicyMetaData(String trust_policy_id)
			throws DirectorException;

	public void updateTrustPolicy(
			UpdateTrustPolicyRequest updateTrustPolicyRequest,
			String trust_policy_id) throws DirectorException;


	public List<TrustPolicyDraft> getTrustPolicyDrafts(
			TrustPolicyDraftFilter trustPolicyDraftFilter)
			throws DirectorException;
	public void writePolicyAndManifest(String policyXml) throws DirectorException;
	
	public String convertPolicyInWindowsFormat(String policyXml) throws DirectorException;
}
