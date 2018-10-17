package com.intel.director.api.ui;

public class SshSettingOrderBy extends OrderBy{

	SshSettingInfoField sshSettingInfoField;
	
	public 	SshSettingInfoField getSshSettingInfoFields() {
        return sshSettingInfoField;
    }

    public void setSshSettingInfoFields(SshSettingInfoField sshSettingInfoField) {
        this.sshSettingInfoField = sshSettingInfoField;
    }
	
}
