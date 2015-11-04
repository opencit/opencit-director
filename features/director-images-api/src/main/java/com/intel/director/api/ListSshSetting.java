package com.intel.director.api;

import java.util.List;
import com.intel.director.api.SshSettingRequest;

public class ListSshSetting extends MonitorStatus{
	
	  public List<SshSettingRequest> sshSettings;

	public List<SshSettingRequest> getSshSettings() {
		return sshSettings;
	}

	public void setSshSettings(List<SshSettingRequest> sshSettings) {
		this.sshSettings = sshSettings;
	}

	
	  
	  
}
