package com.intel.director.api.ui;

import java.util.Date;

import com.intel.director.api.PolicyUploadTransferObject;

public class PolicyUploadFilter extends PolicyUploadTransferObject{
	
	protected Date from_date;

    protected Date to_date;

    protected String trust_policy_id;

    protected boolean  enableDeletedCheck;
    
    public boolean isEnableDeletedCheck() {
		return enableDeletedCheck;
	}
	public void setEnableDeletedCheck(boolean enableDeletedCheck) {
		this.enableDeletedCheck = enableDeletedCheck;
	}
    
	public Date getFrom_date() {
		return from_date;
	}

	public void setFrom_date(Date from_date) {
		this.from_date = from_date;
	}

	public Date getTo_date() {
		return to_date;
	}

	public void setTo_date(Date to_date) {
		this.to_date = to_date;
	}

	public String getTrust_policy_id() {
		return trust_policy_id;
	}

	public void setTrust_policy_id(String trust_policy_id) {
		this.trust_policy_id = trust_policy_id;
	}

    
    
}
